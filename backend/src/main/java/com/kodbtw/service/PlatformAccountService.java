package com.kodbtw.service;

import com.kodbtw.dto.PlatformAccountRequest;
import com.kodbtw.dto.PlatformAccountResponse;
import com.kodbtw.entity.PlatformAccount;
import com.kodbtw.entity.User;
import com.kodbtw.exception.DuplicatePlatformException;
import com.kodbtw.exception.ResourceNotFoundException;
import com.kodbtw.repository.PlatformAccountRepository;
import com.kodbtw.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class PlatformAccountService {

    private final PlatformAccountRepository platformAccountRepository;
    private final UserRepository userRepository;

    public PlatformAccountService(PlatformAccountRepository platformAccountRepository,
                                  UserRepository userRepository) {
        this.platformAccountRepository = platformAccountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PlatformAccountResponse create(Long userId, PlatformAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (platformAccountRepository.existsByUserIdAndPlatform(userId, request.getPlatform())) {
            throw new DuplicatePlatformException(
                    "You have already linked a " + request.getPlatform() + " account");
        }

        String profileUrl = (request.getProfileUrl() != null && request.getProfileUrl().isBlank())
                ? null : request.getProfileUrl();

        PlatformAccount account = new PlatformAccount(
                user, request.getPlatform(), request.getUsername().trim(), profileUrl);
        PlatformAccount saved = platformAccountRepository.save(account);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PlatformAccountResponse> getAll(Long userId) {
        return platformAccountRepository.findAllByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlatformAccountResponse getOne(Long userId, Long accountId) {
        PlatformAccount account = platformAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Platform account not found"));
        return toResponse(account);
    }

    @Transactional
    public PlatformAccountResponse update(Long userId, Long accountId, PlatformAccountRequest request) {
        PlatformAccount account = platformAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Platform account not found"));

        // If platform is changing, check for duplicate
        if (request.getPlatform() != null && !request.getPlatform().equals(account.getPlatform())) {
            if (platformAccountRepository.existsByUserIdAndPlatform(userId, request.getPlatform())) {
                throw new DuplicatePlatformException(
                        "You have already linked a " + request.getPlatform() + " account");
            }
            account.setPlatform(request.getPlatform());
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            account.setUsername(request.getUsername().trim());
        }
        if (request.getProfileUrl() != null) {
            account.setProfileUrl(request.getProfileUrl().isBlank() ? null : request.getProfileUrl());
        }

        PlatformAccount saved = platformAccountRepository.save(account);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long userId, Long accountId) {
        PlatformAccount account = platformAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Platform account not found"));
        platformAccountRepository.delete(account);
    }

    private PlatformAccountResponse toResponse(PlatformAccount account) {
        PlatformAccountResponse response = new PlatformAccountResponse(
                account.getId(),
                account.getUser().getId(),
                account.getPlatform(),
                account.getUsername(),
                account.getProfileUrl(),
                account.getVerified(),
                account.getConnectedAt(),
                account.getUpdatedAt()
        );
        response.setSyncStatus(account.getSyncStatus());
        response.setLastAttemptAt(account.getLastAttemptAt());
        response.setLastSuccessAt(account.getLastSuccessAt());
        response.setLastFailureAt(account.getLastFailureAt());
        response.setLastSyncErrorCategory(account.getLastSyncErrorCategory());
        response.setLastSyncErrorMessage(account.getLastSyncErrorMessage());
        response.setFresh(account.getLastSuccessAt() != null
                && Duration.between(account.getLastSuccessAt(), LocalDateTime.now(ZoneOffset.UTC)).compareTo(Duration.ofHours(24)) <= 0);
        return response;
    }
}
