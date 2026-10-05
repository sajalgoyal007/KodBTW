package com.kodbtw.service;

import com.kodbtw.dto.ProfileRequest;
import com.kodbtw.dto.ProfileResponse;
import com.kodbtw.entity.Profile;
import com.kodbtw.entity.User;
import com.kodbtw.exception.ResourceNotFoundException;
import com.kodbtw.repository.ProfileRepository;
import com.kodbtw.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProfileService {

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

        applyRequest(profile, request);
        Profile saved = profileRepository.save(profile);
        return toResponse(saved);
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
