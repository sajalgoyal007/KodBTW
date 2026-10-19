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
 * applies V7 through V11 and validates the resulting schema against the application's JPA entities.
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

            statement.executeUpdate("INSERT INTO profiles (user_id, username) VALUES "
                    + "(101, 'migration-test-101')");

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
    void appliesV7ThroughV11PreservingLegacyRowsAndCreatingGoogleAuthSchema() {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_stat_snapshots", Integer.class)).isEqualTo(5);

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class)).isEqualTo(2);
        Map<String, Object> legacyUser = jdbc.queryForMap(
                "SELECT id, name, email, password FROM users WHERE id = 101");
        assertThat(legacyUser).containsEntry("id", 101L)
                .containsEntry("name", "Migration Test")
                .containsEntry("email", "migration-test-101@example.invalid")
                .containsEntry("password", "not-a-login");
        Map<String, Object> secondLegacyUser = jdbc.queryForMap(
                "SELECT id, name, email, password FROM users WHERE id = 102");
        assertThat(secondLegacyUser).containsEntry("id", 102L)
                .containsEntry("name", "Migration Test 2")
                .containsEntry("email", "migration-test-102@example.invalid")
                .containsEntry("password", "not-a-login");

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
        assertThat(applied).hasSize(11);
        assertThat(applied).extracting(row -> row.get("version"))
                .containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
        assertThat(applied).allSatisfy(row -> assertThat(row.get("success")).isEqualTo(true));
        assertThat(jdbc.queryForObject(
                "SELECT indexable FROM profiles WHERE user_id = 101", Boolean.class)).isFalse();
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() "
                        + "AND table_name = 'platform_stat_snapshots' AND column_name = 'max_rating' "
                        + "AND is_nullable = 'YES' AND data_type = 'int'", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() "
                        + "AND table_name = 'profiles' AND column_name = 'indexable' "
                        + "AND is_nullable = 'NO' AND data_type = 'tinyint'", Integer.class)).isEqualTo(1);

        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() "
                        + "AND table_name = 'users' AND column_name = 'password' "
                        + "AND data_type = 'varchar' AND character_maximum_length = 255 AND is_nullable = 'YES'",
                Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() "
                        + "AND table_name = 'user_auth_identities'", Integer.class)).isEqualTo(1);
        assertColumn("user_auth_identities", "id", "bigint", "NO");
        assertColumn("user_auth_identities", "user_id", "bigint", "NO");
        assertColumn("user_auth_identities", "provider", "varchar", "NO");
        assertColumn("user_auth_identities", "provider_subject", "varchar", "NO");
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.key_column_usage WHERE table_schema = DATABASE() "
                        + "AND table_name = 'user_auth_identities' "
                        + "AND constraint_name = 'fk_user_auth_identity_user' "
                        + "AND column_name = 'user_id' AND referenced_table_name = 'users' "
                        + "AND referenced_column_name = 'id'", Integer.class)).isEqualTo(1);
        assertUniqueIndexColumns("user_auth_identities", "uk_user_auth_provider_subject",
                List.of("provider", "provider_subject"));
        assertUniqueIndexColumns("user_auth_identities", "uk_user_auth_user_provider",
                List.of("user_id", "provider"));

        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() "
                        + "AND table_name = 'google_auth_challenges'", Integer.class)).isEqualTo(1);
        assertColumn("google_auth_challenges", "challenge_id", "varchar", "NO");
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() "
                        + "AND table_name = 'google_auth_challenges' AND column_name = 'challenge_id' "
                        + "AND character_maximum_length = 36", Integer.class)).isEqualTo(1);
        assertColumn("google_auth_challenges", "nonce_hash", "binary", "NO");
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() "
                        + "AND table_name = 'google_auth_challenges' AND column_name = 'nonce_hash' "
                        + "AND character_maximum_length = 32", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() "
                        + "AND table_name = 'google_auth_challenges' AND column_name = 'expires_at' "
                        + "AND data_type = 'timestamp' AND datetime_precision = 6 AND is_nullable = 'NO'",
                Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() "
                        + "AND table_name = 'google_auth_challenges' AND column_name = 'created_at' "
                        + "AND data_type = 'timestamp' AND datetime_precision = 6 AND is_nullable = 'NO'",
                Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() "
                        + "AND table_name = 'google_auth_challenges' AND index_name = 'PRIMARY' "
                        + "AND column_name = 'challenge_id' AND non_unique = 0", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() "
                        + "AND table_name = 'google_auth_challenges' "
                        + "AND index_name = 'idx_google_auth_challenges_expires_at' "
                        + "AND column_name = 'expires_at' AND seq_in_index = 1 AND non_unique = 1",
                Integer.class)).isEqualTo(1);
        assertColumn("google_auth_challenges", "operation", "varchar", "NO");
        assertColumn("google_auth_challenges", "user_id", "bigint", "YES");
        assertThat(jdbc.queryForObject(
                "SELECT column_default FROM information_schema.columns WHERE table_schema = DATABASE() "
                        + "AND table_name = 'google_auth_challenges' AND column_name = 'operation'",
                String.class)).isEqualTo("LOGIN");
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.key_column_usage WHERE table_schema = DATABASE() "
                        + "AND table_name = 'google_auth_challenges' "
                        + "AND constraint_name = 'fk_google_auth_challenge_user' "
                        + "AND column_name = 'user_id' AND referenced_table_name = 'users' "
                        + "AND referenced_column_name = 'id'", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() "
                        + "AND table_name = 'google_auth_challenges' "
                        + "AND index_name = 'idx_google_auth_challenges_operation_user_expiry' "
                        + "AND column_name = 'operation' AND seq_in_index = 1 AND non_unique = 1",
                Integer.class)).isEqualTo(1);
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

    private void assertColumn(String table, String column, String dataType, String nullable) {
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() "
                        + "AND table_name = ? AND column_name = ? AND data_type = ? AND is_nullable = ?",
                Integer.class, table, column, dataType, nullable)).isEqualTo(1);
    }

    private void assertUniqueIndexColumns(String table, String index, List<String> columns) {
        List<Map<String, Object>> actual = jdbc.queryForList(
                "SELECT column_name FROM information_schema.statistics WHERE table_schema = DATABASE() "
                        + "AND table_name = ? AND index_name = ? AND non_unique = 0 ORDER BY seq_in_index",
                table, index);
        assertThat(actual).extracting(row -> row.get("column_name"))
                .containsExactlyElementsOf(columns);
    }
}
