package com.kodbtw.dto;

import com.kodbtw.entity.Platform;

import java.time.LocalDateTime;

public class PlatformAccountResponse {

    private Long id;
    private Long userId;
    private Platform platform;
    private String username;
    private String profileUrl;
    private Boolean verified;
    private LocalDateTime connectedAt;
    private LocalDateTime updatedAt;

    public PlatformAccountResponse() {
    }

    public PlatformAccountResponse(Long id, Long userId, Platform platform, String username,
                                   String profileUrl, Boolean verified,
                                   LocalDateTime connectedAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.platform = platform;
        this.username = username;
        this.profileUrl = profileUrl;
        this.verified = verified;
        this.connectedAt = connectedAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Platform getPlatform() { return platform; }
    public void setPlatform(Platform platform) { this.platform = platform; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getProfileUrl() { return profileUrl; }
    public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    public LocalDateTime getConnectedAt() { return connectedAt; }
    public void setConnectedAt(LocalDateTime connectedAt) { this.connectedAt = connectedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
