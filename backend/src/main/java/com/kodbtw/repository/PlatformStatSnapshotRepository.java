package com.kodbtw.repository;

import com.kodbtw.entity.PlatformStatSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Repository
public interface PlatformStatSnapshotRepository extends JpaRepository<PlatformStatSnapshot, Long> {

    List<PlatformStatSnapshot> findAllByUserId(Long userId);

    Optional<PlatformStatSnapshot> findFirstByUserIdAndPlatformOrderBySnapshotDateDesc(Long userId, String platform);

    List<PlatformStatSnapshot> findAllByUserIdAndSnapshotDateGreaterThanEqualAndSnapshotDateLessThanEqualAndSourceEndingWithOrderBySnapshotDateAscPlatformAscSnapshottedAtAsc(
            Long userId, LocalDate startDate, LocalDate endDate, String sourceSuffix);

    Optional<PlatformStatSnapshot> findByUserIdAndPlatformAndSnapshotDate(Long userId, String platform, java.time.LocalDate snapshotDate);
}
