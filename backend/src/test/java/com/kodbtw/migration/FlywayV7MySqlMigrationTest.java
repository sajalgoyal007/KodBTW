package com.kodbtw.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the real MySQL migration sequence against a disposable MySQL 8 container.
 * The database is migrated only through V6 and seeded before Spring starts; Spring then
 * applies V7 and validates the resulting schema against the application's JPA entities.
 */
@SpringBootTest
@Testcontainers
class FlywayV7MySqlMigrationTest {

    private static final LocalDate SNAPSHOT_DAY = LocalDate.of(2025, 1, 10);

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.46")
            .withDatabaseName("kodbtw_migration_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private JdbcTemplate jdbc;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        MYSQL.start();
        prepareV6Database();

        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
    }

    private static void prepareV6Database() {
        Flyway.configure()
                .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .locations("classpath:db/migration")
                .target(MigrationVersion.fromVersion("6"))
                .load()
                .migrate();

        try (Connection connection = DriverManager.getConnection(
                MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
             var statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO users (id, name, email, password) VALUES "
                    + "(101, 'Migration Test', 'migration-test-101@example.invalid', 'not-a-login'), "
                    + "(102, 'Migration Test 2', 'migration-test-102@example.invalid', 'not-a-login')");

            statement.executeUpdate("INSERT INTO platform_stat_snapshots "
                    + "(user_id, platform, snapshot_date, total_solved, easy_solved, medium_solved, "
                    + "hard_solved, rating, contests, current_streak, source, platform_rank, "
                    + "longest_streak, stats_last_synced_at) VALUES "
                    + "(101, 'CODEFORCES', '2025-01-10', 200, 80, 70, 50, 1800, 12, 3, "
                    + "'CODEFORCES_REAL', 1900, 8, '2025-01-10 12:00:00'), "
                    + "(101, 'CODEFORCES', '2025-01-09', 195, 78, 68, 49, 1750, 11, 2, "
                    + "'CODEFORCES_REAL', 1850, 7, '2025-01-09 12:00:00'), "
                    + "(101, 'CODEFORCES', '2025-01-08', NULL, NULL, NULL, NULL, NULL, NULL, NULL, "
                    + "'CODEFORCES_REAL', NULL, NULL, NULL), "
                    + "(101, 'CODEFORCES', '2025-01-07', 10, NULL, NULL, NULL, 100, 1, NULL, "
                    + "'MOCK', 123, NULL, NULL), "
                    + "(102, 'CODECHEF', '2025-01-10', 25, 10, 8, 7, 1500, 2, 1, "
                    + "'CODECHEF_REAL', 4, 3, '2025-01-10 12:00:00')");
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not seed disposable MySQL migration database", exception);
        }
    }

    @Test
    void appliesV7PreservingRowsAndOnlySeparatingCodeforcesRealMaxRating() {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_stat_snapshots", Integer.class)).isEqualTo(5);

        Map<String, Object> currentCodeforces = snapshot(101L, "CODEFORCES", SNAPSHOT_DAY);
        assertThat(currentCodeforces.get("platform_rank")).isNull();
        assertThat(currentCodeforces.get("max_rating")).isEqualTo(1900);
        assertThat(currentCodeforces.get("total_solved")).isEqualTo(200);

        Map<String, Object> priorCodeforces = snapshot(101L, "CODEFORCES", SNAPSHOT_DAY.minusDays(1));
        assertThat(priorCodeforces.get("platform_rank")).isNull();
        assertThat(priorCodeforces.get("max_rating")).isEqualTo(1850);

        Map<String, Object> nullableCodeforces = snapshot(101L, "CODEFORCES", SNAPSHOT_DAY.minusDays(2));
        assertThat(nullableCodeforces.get("platform_rank")).isNull();
        assertThat(nullableCodeforces.get("max_rating")).isNull();
        assertThat(nullableCodeforces.get("total_solved")).isNull();

        Map<String, Object> mockCodeforces = snapshot(101L, "CODEFORCES", SNAPSHOT_DAY.minusDays(3));
        assertThat(mockCodeforces.get("platform_rank")).isEqualTo(123);
        assertThat(mockCodeforces.get("max_rating")).isNull();

        Map<String, Object> otherPlatform = snapshot(102L, "CODECHEF", SNAPSHOT_DAY);
        assertThat(otherPlatform.get("platform_rank")).isEqualTo(4);
        assertThat(otherPlatform.get("max_rating")).isNull();

        List<Map<String, Object>> applied = jdbc.queryForList(
                "SELECT version, success FROM flyway_schema_history WHERE type = 'SQL' ORDER BY installed_rank");
        assertThat(applied).hasSize(7);
        assertThat(applied).allSatisfy(row -> assertThat(row.get("success")).isEqualTo(true));
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() "
                        + "AND table_name = 'platform_stat_snapshots' AND column_name = 'max_rating' "
                        + "AND is_nullable = 'YES' AND data_type = 'int'", Integer.class)).isEqualTo(1);
    }

    @Test
    void snapshotUniquenessConstraintStillRejectsDuplicateUserPlatformDate() {
        assertThatThrownBy(() -> jdbc.update("INSERT INTO platform_stat_snapshots "
                        + "(user_id, platform, snapshot_date, source) VALUES (101, 'CODEFORCES', ?, 'MOCK')",
                SNAPSHOT_DAY))
                .isInstanceOf(DuplicateKeyException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_stat_snapshots", Integer.class)).isEqualTo(5);
    }

    private Map<String, Object> snapshot(Long userId, String platform, LocalDate date) {
        return jdbc.queryForMap("SELECT total_solved, platform_rank, max_rating FROM platform_stat_snapshots "
                + "WHERE user_id = ? AND platform = ? AND snapshot_date = ?", userId, platform, date);
    }
}
