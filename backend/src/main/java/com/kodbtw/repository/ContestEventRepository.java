package com.kodbtw.repository;

import com.kodbtw.entity.ContestEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContestEventRepository extends JpaRepository<ContestEvent, Long> {

    Optional<ContestEvent> findByExternalId(String externalId);

    Optional<ContestEvent> findFirstByPlatformAndStartTimeUtcAndDurationSeconds(
            String platform, LocalDateTime startTimeUtc, long durationSeconds);

    List<ContestEvent> findByStartTimeUtcGreaterThanEqualAndStartTimeUtcLessThanOrderByStartTimeUtcAsc(
            LocalDateTime fromInclusive, LocalDateTime toExclusive);
}
