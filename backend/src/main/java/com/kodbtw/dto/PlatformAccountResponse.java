package com.kodbtw.dto;

import com.kodbtw.entity.Platform;

import java.time.LocalDateTime;
import com.kodbtw.entity.SyncStatus;
import com.kodbtw.entity.PlatformSourceStatus;

public class PlatformAccountResponse {

    private Long id;
    private Long userId;
    private Platform platform;
    private String username;
    private String profileUrl;
    private Boolean verified;
    private LocalDateTime connectedAt;
    private LocalDateTime updatedAt;
    private SyncStatus syncStatus;
    private LocalDateTime lastAttemptAt;
    private LocalDateTime lastSuccessAt;
    private LocalDateTime lastFailureAt;
    private String lastSyncErrorCategory;
    private String lastSyncErrorMessage;
    private boolean fresh;
    private PlatformSourceStatus sourceStatus;

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
    public SyncStatus getSyncStatus() { return syncStatus; }
    public void setSyncStatus(SyncStatus value) { this.syncStatus = value; }
    public LocalDateTime getLastAttemptAt() { return lastAttemptAt; }
    public void setLastAttemptAt(LocalDateTime value) { this.lastAttemptAt = value; }
    public LocalDateTime getLastSuccessAt() { return lastSuccessAt; }
    public void setLastSuccessAt(LocalDateTime value) { this.lastSuccessAt = value; }
    public LocalDateTime getLastFailureAt() { return lastFailureAt; }
    public void setLastFailureAt(LocalDateTime value) { this.lastFailureAt = value; }
    public String getLastSyncErrorCategory() { return lastSyncErrorCategory; }
    public void setLastSyncErrorCategory(String value) { this.lastSyncErrorCategory = value; }
    public String getLastSyncErrorMessage() { return lastSyncErrorMessage; }
    public void setLastSyncErrorMessage(String value) { this.lastSyncErrorMessage = value; }
    public boolean isFresh() { return fresh; }
    public void setFresh(boolean value) { this.fresh = value; }
    public PlatformSourceStatus getSourceStatus() { return sourceStatus; }
    public void setSourceStatus(PlatformSourceStatus value) { this.sourceStatus = value; }
}
