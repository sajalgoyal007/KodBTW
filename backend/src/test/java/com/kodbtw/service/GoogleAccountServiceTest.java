package com.kodbtw.service;

import com.kodbtw.entity.User;
import com.kodbtw.entity.UserAuthIdentity;
import com.kodbtw.exception.GoogleAccountExistsException;
import com.kodbtw.repository.UserAuthIdentityRepository;
import com.kodbtw.repository.UserRepository;
import org.junit.jupiter.api.Test;

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
}
