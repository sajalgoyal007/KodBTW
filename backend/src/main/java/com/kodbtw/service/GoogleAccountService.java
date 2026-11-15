package com.kodbtw.service;

import com.kodbtw.entity.User;
import com.kodbtw.entity.UserAuthIdentity;
import com.kodbtw.exception.GoogleAccountExistsException;
import com.kodbtw.exception.GoogleIdentityConflictException;
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
        if (linked.isPresent()) {
            User user = linked.get().getUser();
            if (!user.isEmailVerified()) {
                user.setEmailVerified(true);
                user.setEmailVerifiedAt(java.time.LocalDateTime.now());
                users.save(user);
            }
            return user;
        }

        String email = verified.email().trim().toLowerCase(Locale.ROOT);
        if (users.findByEmail(email).isPresent()) throw new GoogleAccountExistsException();

        User newUser = new User(verified.name(), email, null, true);
        User user = users.saveAndFlush(newUser);
        identities.saveAndFlush(new UserAuthIdentity(user, PROVIDER, verified.subject()));
        return user;
    }

    @Transactional(readOnly = true)
    public Optional<User> findLinkedUser(String subject) {
        return identities.findByProviderAndProviderSubject(PROVIDER, subject).map(UserAuthIdentity::getUser);
    }

    @Transactional(readOnly = true)
    public boolean hasGoogleIdentity(Long userId) {
        return userId != null && identities.existsByUser_IdAndProvider(userId, PROVIDER);
    }

    @Transactional
    public void linkGoogleIdentity(User user, VerifiedGoogleIdentity verified) {
        if (user == null || user.getId() == null) throw new GoogleIdentityConflictException();

        Optional<UserAuthIdentity> identityForSubject = identities.findByProviderAndProviderSubject(
                PROVIDER, verified.subject());
        if (identityForSubject.isPresent()) {
            if (identityForSubject.get().getUser().getId().equals(user.getId())) return;
            throw new GoogleIdentityConflictException();
        }

        if (identities.findByUser_IdAndProvider(user.getId(), PROVIDER).isPresent()) {
            throw new GoogleIdentityConflictException();
        }

        try {
            identities.saveAndFlush(new UserAuthIdentity(user, PROVIDER, verified.subject()));
            if (!user.isEmailVerified()) {
                user.setEmailVerified(true);
                user.setEmailVerifiedAt(java.time.LocalDateTime.now());
                users.save(user);
            }
        } catch (DataIntegrityViolationException concurrentLink) {
            // Database uniqueness constraints are the atomic guard against concurrent link attempts.
            throw new GoogleIdentityConflictException();
        }
    }
}
