package com.kodbtw.dto;

public class ProfileResponse {

    private Long id;
    private Long userId;
    private String username;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private String college;
    private Integer graduationYear;
    private String location;
    private String githubUrl;
    private String linkedinUrl;
    private String portfolioUrl;

    public ProfileResponse() {
    }

    public ProfileResponse(Long id, Long userId, String username, String displayName, String bio,
                           String avatarUrl, String college, Integer graduationYear,
                           String location, String githubUrl, String linkedinUrl,
                           String portfolioUrl) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.college = college;
        this.graduationYear = graduationYear;
        this.location = location;
        this.githubUrl = githubUrl;
        this.linkedinUrl = linkedinUrl;
        this.portfolioUrl = portfolioUrl;
    }

    public ProfileResponse(Long id, Long userId, String displayName, String bio,
                           String avatarUrl, String college, Integer graduationYear,
                           String location, String githubUrl, String linkedinUrl,
                           String portfolioUrl) {
        this(id, userId, null, displayName, bio, avatarUrl, college, graduationYear, location, githubUrl, linkedinUrl, portfolioUrl);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }
}
