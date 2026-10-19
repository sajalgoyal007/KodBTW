package com.kodbtw.repository;

import com.kodbtw.entity.UserAuthIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserAuthIdentityRepository extends JpaRepository<UserAuthIdentity, Long> {
    Optional<UserAuthIdentity> findByProviderAndProviderSubject(String provider, String providerSubject);
}
