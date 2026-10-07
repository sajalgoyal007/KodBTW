package com.kodbtw.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.Locale;

public class ProfileRequest {

    @Pattern(regexp = "^[a-z0-9_-]{3,50}$", message = "Username must be 3-50 characters with lowercase letters, numbers, hyphens, and underscores only")
    private String username;

    @Size(max = 100, message = "Display name must not exceed 100 characters")
    private String displayName;

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;

    @URL(message = "Avatar URL must be a valid URL")
    @Size(max = 500)
    private String avatarUrl;

    @Size(max = 255, message = "College must not exceed 255 characters")
    private String college;

    @Min(value = 2000, message = "Graduation year must be 2000 or later")
    @Max(value = 2040, message = "Graduation year must be 2040 or earlier")
    private Integer graduationYear;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    @URL(message = "GitHub URL must be a valid URL")
    @Size(max = 500)
    private String githubUrl;

    @URL(message = "LinkedIn URL must be a valid URL")
    @Size(max = 500)
    private String linkedinUrl;

    @URL(message = "Portfolio URL must be a valid URL")
    @Size(max = 500)
    private String portfolioUrl;

    public ProfileRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username.trim().toLowerCase(Locale.ROOT) : null;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(Integer graduationYear) {
        this.graduationYear = graduationYear;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getPortfolioUrl() {
        return portfolioUrl;
    }

    public void setPortfolioUrl(String portfolioUrl) {
        this.portfolioUrl = portfolioUrl;
    }
}
