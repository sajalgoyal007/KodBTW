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
}
