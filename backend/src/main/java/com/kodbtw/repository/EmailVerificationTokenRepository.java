package com.kodbtw.repository;

import com.kodbtw.entity.EmailVerificationToken;
import com.kodbtw.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    Optional<EmailVerificationToken> findTopByUserOrderByCreatedAtDesc(User user);

    Optional<EmailVerificationToken> findTopByUser_IdOrderByCreatedAtDesc(Long userId);

    void deleteByUser(User user);
}
