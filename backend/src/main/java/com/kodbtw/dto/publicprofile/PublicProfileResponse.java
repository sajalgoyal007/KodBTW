package com.kodbtw.dto.publicprofile;

import java.util.ArrayList;
import java.util.List;

public class PublicProfileResponse {

    private String username;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private String college;
    private Integer graduationYear;
    private String location;
    private PublicSocialLinksDto socialLinks;
    private PublicOverviewDto overview;
    private List<PublicPlatformStatDto> platforms;
    private PublicAnalyticsDto analytics;

    public PublicProfileResponse() {
        this.platforms = new ArrayList<>();
    }

    public PublicProfileResponse(String username, String displayName, String bio, String avatarUrl,
                                 String college, Integer graduationYear, String location,
                                 PublicSocialLinksDto socialLinks, PublicOverviewDto overview,
                                 List<PublicPlatformStatDto> platforms, PublicAnalyticsDto analytics) {
        this.username = username;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.college = college;
        this.graduationYear = graduationYear;
        this.location = location;
        this.socialLinks = socialLinks;
        this.overview = overview;
        this.platforms = platforms != null ? platforms : new ArrayList<>();
        this.analytics = analytics;
    }

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

    public PublicSocialLinksDto getSocialLinks() { return socialLinks; }
    public void setSocialLinks(PublicSocialLinksDto socialLinks) { this.socialLinks = socialLinks; }

    public PublicOverviewDto getOverview() { return overview; }
    public void setOverview(PublicOverviewDto overview) { this.overview = overview; }

    public List<PublicPlatformStatDto> getPlatforms() { return platforms; }
    public void setPlatforms(List<PublicPlatformStatDto> platforms) {
        this.platforms = platforms != null ? platforms : new ArrayList<>();
    }

    public PublicAnalyticsDto getAnalytics() { return analytics; }
    public void setAnalytics(PublicAnalyticsDto analytics) { this.analytics = analytics; }
}
