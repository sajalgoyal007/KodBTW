package com.kodbtw.service;

import com.kodbtw.entity.User;
import com.kodbtw.entity.UserAuthIdentity;
import com.kodbtw.exception.GoogleAccountExistsException;
import com.kodbtw.repository.UserAuthIdentityRepository;
import com.kodbtw.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
public class GoogleAccountService {
    private static final String PROVIDER = "GOOGLE";
    private final UserRepository users;
    private final UserAuthIdentityRepository identities;

    public GoogleAccountService(UserRepository users, UserAuthIdentityRepository identities) {
        this.users = users;
        this.identities = identities;
    }

    @Transactional
    public User resolveOrCreate(VerifiedGoogleIdentity verified) {
        Optional<UserAuthIdentity> linked = identities.findByProviderAndProviderSubject(PROVIDER, verified.subject());
        if (linked.isPresent()) return linked.get().getUser();

        String email = verified.email().trim().toLowerCase(Locale.ROOT);
        if (users.findByEmail(email).isPresent()) throw new GoogleAccountExistsException();

        User user = users.saveAndFlush(new User(verified.name(), email, null));
        identities.saveAndFlush(new UserAuthIdentity(user, PROVIDER, verified.subject()));
        return user;
    }

    @Transactional(readOnly = true)
    public Optional<User> findLinkedUser(String subject) {
        return identities.findByProviderAndProviderSubject(PROVIDER, subject).map(UserAuthIdentity::getUser);
    }
}
