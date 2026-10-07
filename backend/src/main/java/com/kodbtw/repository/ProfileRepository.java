package com.kodbtw.repository;

import com.kodbtw.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Collection;
import java.util.List;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    Optional<Profile> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndUserIdNot(String username, Long userId);

    List<Profile> findAllByUserIdIn(Collection<Long> userIds);
}
