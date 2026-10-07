package com.kodbtw.service;

import com.kodbtw.dto.ProfileRequest;
import com.kodbtw.dto.ProfileResponse;
import com.kodbtw.entity.Profile;
import com.kodbtw.entity.User;
import com.kodbtw.exception.ResourceNotFoundException;
import com.kodbtw.exception.UsernameAlreadyExistsException;
import com.kodbtw.repository.ProfileRepository;
import com.kodbtw.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class ProfileService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9_-]{3,50}$");

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for the current user"));
        return toResponse(profile);
    }

    @Transactional
    public ProfileResponse upsertProfile(Long userId, ProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Profile profile = profileRepository.findByUserId(userId)
                .orElse(new Profile(user));

        handleUsername(profile, user, request);
        applyRequest(profile, request);
        Profile saved = profileRepository.save(profile);
        return toResponse(saved);
    }

    private void handleUsername(Profile profile, User user, ProfileRequest request) {
        String reqUsername = request.getUsername();
        if (reqUsername != null && !reqUsername.isBlank()) {
            String normalized = reqUsername.trim().toLowerCase(Locale.ROOT);
            if (!USERNAME_PATTERN.matcher(normalized).matches()) {
                throw new IllegalArgumentException("Username must be 3-50 characters with lowercase letters, numbers, hyphens, and underscores only");
            }
            if (profileRepository.existsByUsernameAndUserIdNot(normalized, user.getId())) {
                throw new UsernameAlreadyExistsException("Username '" + normalized + "' is already taken");
            }
            profile.setUsername(normalized);
        } else if (profile.getUsername() == null || profile.getUsername().isBlank()) {
            String safeUsername = generateSafeUsername(user, request);
            profile.setUsername(safeUsername);
        }
    }

    private String generateSafeUsername(User user, ProfileRequest request) {
        String baseRaw = (request.getDisplayName() != null && !request.getDisplayName().isBlank())
                ? request.getDisplayName()
                : (user.getName() != null && !user.getName().isBlank() ? user.getName() : "user");

        String sanitized = baseRaw.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "-").replaceAll("-+", "-");
        if (sanitized.startsWith("-")) sanitized = sanitized.substring(1);
        if (sanitized.endsWith("-")) sanitized = sanitized.substring(0, sanitized.length() - 1);
        if (sanitized.length() < 3) {
            sanitized = "user-" + user.getId();
        }
        if (sanitized.length() > 40) {
            sanitized = sanitized.substring(0, 40);
        }

        if (!profileRepository.existsByUsername(sanitized)) {
            return sanitized;
        }

        String userIdSuffix = "-" + user.getId();
        String candidateBase = sanitized.substring(0, Math.min(sanitized.length(), 50 - userIdSuffix.length()));
        String candidateWithId = candidateBase + userIdSuffix;
        if (!profileRepository.existsByUsername(candidateWithId)) {
            return candidateWithId;
        }

        for (int suffix = 2; ; suffix++) {
            String collisionSuffix = userIdSuffix + "-" + suffix;
            String candidate = sanitized.substring(0, Math.min(sanitized.length(), 50 - collisionSuffix.length())) + collisionSuffix;
            if (!profileRepository.existsByUsername(candidate)) {
                return candidate;
            }
        }
    }

    private void applyRequest(Profile profile, ProfileRequest request) {
        if (request.getDisplayName() != null) profile.setDisplayName(request.getDisplayName());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(emptyToNull(request.getAvatarUrl()));
        if (request.getCollege() != null) profile.setCollege(request.getCollege());
        if (request.getGraduationYear() != null) profile.setGraduationYear(request.getGraduationYear());
        if (request.getLocation() != null) profile.setLocation(request.getLocation());
        if (request.getGithubUrl() != null) profile.setGithubUrl(emptyToNull(request.getGithubUrl()));
        if (request.getLinkedinUrl() != null) profile.setLinkedinUrl(emptyToNull(request.getLinkedinUrl()));
        if (request.getPortfolioUrl() != null) profile.setPortfolioUrl(emptyToNull(request.getPortfolioUrl()));
    }

    private String emptyToNull(String value) {
        return (value != null && value.isBlank()) ? null : value;
    }

    private ProfileResponse toResponse(Profile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUsername(),
                profile.getDisplayName(),
                profile.getBio(),
                profile.getAvatarUrl(),
                profile.getCollege(),
                profile.getGraduationYear(),
                profile.getLocation(),
                profile.getGithubUrl(),
                profile.getLinkedinUrl(),
                profile.getPortfolioUrl()
        );
    }
}
