package com.kodbtw.repository;

import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformAccountRepository extends JpaRepository<PlatformAccount, Long> {

    List<PlatformAccount> findAllByUserId(Long userId);

    Optional<PlatformAccount> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndPlatform(Long userId, Platform platform);

    /**
     * Returns all distinct user IDs that have at least one connected platform account.
     * Used by the leaderboard sync job to iterate all users that need snapshots.
     */
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT pa.user.id FROM PlatformAccount pa ORDER BY pa.user.id ASC")
    java.util.List<Long> findAllDistinctUserIds();

    @org.springframework.data.jpa.repository.Query("SELECT pa.id FROM PlatformAccount pa ORDER BY pa.id ASC")
    java.util.List<Long> findAllAccountIds();

    @org.springframework.data.jpa.repository.Query("SELECT pa.user.id FROM PlatformAccount pa WHERE pa.id = :accountId")
    java.util.Optional<Long> findUserIdByAccountId(@org.springframework.data.repository.query.Param("accountId") Long accountId);

    @org.springframework.data.jpa.repository.Query("SELECT pa.id FROM PlatformAccount pa WHERE pa.user.id = :userId ORDER BY pa.id ASC")
    java.util.List<Long> findAccountIdsByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);
}
