package com.kodbtw.service;

import com.kodbtw.entity.User;
import com.kodbtw.entity.UserAuthIdentity;
import com.kodbtw.exception.GoogleAccountExistsException;
import com.kodbtw.exception.GoogleIdentityConflictException;
import com.kodbtw.repository.UserAuthIdentityRepository;
import com.kodbtw.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GoogleAccountServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final UserAuthIdentityRepository identities = mock(UserAuthIdentityRepository.class);
    private final GoogleAccountService service = new GoogleAccountService(users, identities);

    @Test
    void existingGoogleSubjectResolvesToItsLinkedUser() {
        User user = new User("Name", "account@example.com", null);
        when(identities.findByProviderAndProviderSubject("GOOGLE", "google-sub-1"))
                .thenReturn(Optional.of(new UserAuthIdentity(user, "GOOGLE", "google-sub-1")));

        assertSame(user, service.resolveOrCreate(new VerifiedGoogleIdentity(
                "google-sub-1", "changed@example.com", "Updated Name", "nonce")));
        verify(users, never()).findByEmail(any());
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void existingEmailIsNotAutomaticallyLinkedToGoogle() {
        when(identities.findByProviderAndProviderSubject("GOOGLE", "new-sub"))
                .thenReturn(Optional.empty());
        when(users.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(new User("Existing", "existing@example.com", "password-hash")));

        assertThrows(GoogleAccountExistsException.class, () -> service.resolveOrCreate(new VerifiedGoogleIdentity(
                "new-sub", "Existing@Example.com", "Existing", "nonce")));
        verify(users, never()).saveAndFlush(any());
        verify(identities, never()).saveAndFlush(any());
    }

    @Test
    void newGoogleIdentityCreatesPasswordlessUserAndUniqueProviderLink() {
        when(identities.findByProviderAndProviderSubject("GOOGLE", "new-sub"))
                .thenReturn(Optional.empty());
        when(users.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(users.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(17L);
            return saved;
        });
        when(identities.saveAndFlush(any(UserAuthIdentity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.resolveOrCreate(new VerifiedGoogleIdentity(
                "new-sub", "New@Example.com", "New User", "nonce"));

        assertEquals(17L, result.getId());
        assertEquals("new@example.com", result.getEmail());
        assertNull(result.getPassword());
        verify(identities).saveAndFlush(argThat(identity -> identity.getProvider().equals("GOOGLE")
                && identity.getProviderSubject().equals("new-sub")));
    }

    @Test
    void linksVerifiedSubjectToAuthenticatedUserWithoutMatchingEmail() {
        User currentUser = new User("Existing", "existing@example.com", "password-hash");
        currentUser.setId(31L);
        VerifiedGoogleIdentity identity = new VerifiedGoogleIdentity(
                "google-sub-31", "different-email@example.com", "Google Name", "nonce");
        when(identities.findByProviderAndProviderSubject("GOOGLE", "google-sub-31"))
                .thenReturn(Optional.empty());
        when(identities.findByUser_IdAndProvider(31L, "GOOGLE")).thenReturn(Optional.empty());

        service.linkGoogleIdentity(currentUser, identity);

        verify(identities).saveAndFlush(argThat(link -> link.getUser() == currentUser
                && link.getProvider().equals("GOOGLE") && link.getProviderSubject().equals("google-sub-31")));
        verifyNoInteractions(users);
    }

    @Test
    void refusesGoogleIdentityAlreadyOwnedByAnotherUser() {
        User otherUser = new User("Other", "other@example.com", null);
        otherUser.setId(32L);
        when(identities.findByProviderAndProviderSubject("GOOGLE", "claimed-sub"))
                .thenReturn(Optional.of(new UserAuthIdentity(otherUser, "GOOGLE", "claimed-sub")));
        User currentUser = new User("Current", "current@example.com", "hash");
        currentUser.setId(31L);

        assertThrows(GoogleIdentityConflictException.class, () -> service.linkGoogleIdentity(currentUser,
                new VerifiedGoogleIdentity("claimed-sub", "other@example.com", "Other", "nonce")));
        verify(identities, never()).saveAndFlush(any());
    }

    @Test
    void doesNotReplaceCurrentUsersDifferentGoogleIdentity() {
        User currentUser = new User("Current", "current@example.com", "hash");
        currentUser.setId(31L);
        when(identities.findByProviderAndProviderSubject("GOOGLE", "new-sub")).thenReturn(Optional.empty());
        when(identities.findByUser_IdAndProvider(31L, "GOOGLE"))
                .thenReturn(Optional.of(new UserAuthIdentity(currentUser, "GOOGLE", "old-sub")));

        assertThrows(GoogleIdentityConflictException.class, () -> service.linkGoogleIdentity(currentUser,
                new VerifiedGoogleIdentity("new-sub", "current@example.com", "Current", "nonce")));
        verify(identities, never()).saveAndFlush(any());
    }

    @Test
    void connectedStatusUsesIdentityRelationship() {
        when(identities.existsByUser_IdAndProvider(31L, "GOOGLE")).thenReturn(true);
        assertTrue(service.hasGoogleIdentity(31L));
        assertFalse(service.hasGoogleIdentity(null));
    }

    @Test
    void uniqueConstraintRaceIsReturnedAsSafeConflict() {
        User currentUser = new User("Current", "current@example.com", "hash");
        currentUser.setId(31L);
        when(identities.findByProviderAndProviderSubject("GOOGLE", "new-sub")).thenReturn(Optional.empty());
        when(identities.findByUser_IdAndProvider(31L, "GOOGLE")).thenReturn(Optional.empty());
        when(identities.saveAndFlush(any(UserAuthIdentity.class)))
                .thenThrow(new DataIntegrityViolationException("constraint details"));

        assertThrows(GoogleIdentityConflictException.class, () -> service.linkGoogleIdentity(currentUser,
                new VerifiedGoogleIdentity("new-sub", "current@example.com", "Current", "nonce")));
    }
}
