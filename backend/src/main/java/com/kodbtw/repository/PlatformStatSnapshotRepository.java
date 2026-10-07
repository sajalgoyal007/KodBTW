package com.kodbtw.repository;

import com.kodbtw.entity.PlatformStatSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformStatSnapshotRepository extends JpaRepository<PlatformStatSnapshot, Long> {

    List<PlatformStatSnapshot> findAllByUserId(Long userId);

    Optional<PlatformStatSnapshot> findFirstByUserIdAndPlatformOrderBySnapshotDateDesc(Long userId, String platform);

    Optional<PlatformStatSnapshot> findByUserIdAndPlatformAndSnapshotDate(Long userId, String platform, java.time.LocalDate snapshotDate);

    /**
     * Upsert a snapshot — update all fields if the (user_id, platform) pair already exists,
     * otherwise insert a new row.
     *
     * <p>Uses MySQL ON DUPLICATE KEY UPDATE semantics via native query.
     */
    @Modifying
    @Query(value = """
            INSERT INTO platform_stat_snapshots
                (user_id, platform, snapshot_date, total_solved, easy_solved, medium_solved, hard_solved,
                 rating, contests, current_streak, source, snapshotted_at)
            VALUES
                (:userId, :platform, CURRENT_DATE, :totalSolved, :easySolved, :mediumSolved, :hardSolved,
                 :rating, :contests, :currentStreak, :source, NOW())
            ON DUPLICATE KEY UPDATE
                total_solved   = VALUES(total_solved),
                easy_solved    = VALUES(easy_solved),
                medium_solved  = VALUES(medium_solved),
                hard_solved    = VALUES(hard_solved),
                rating         = VALUES(rating),
                contests       = VALUES(contests),
                current_streak = VALUES(current_streak),
                source         = VALUES(source),
                snapshotted_at = NOW()
            """, nativeQuery = true)
    void upsert(
            @Param("userId") Long userId,
            @Param("platform") String platform,
            @Param("totalSolved") Integer totalSolved,
            @Param("easySolved") Integer easySolved,
            @Param("mediumSolved") Integer mediumSolved,
            @Param("hardSolved") Integer hardSolved,
            @Param("rating") Integer rating,
            @Param("contests") Integer contests,
            @Param("currentStreak") Integer currentStreak,
            @Param("source") String source
    );
}
