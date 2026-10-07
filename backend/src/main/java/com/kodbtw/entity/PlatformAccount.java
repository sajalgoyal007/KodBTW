package com.kodbtw.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import com.kodbtw.entity.SyncStatus;

@Entity
@Table(
        name = "platform_accounts",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_platform", columnNames = {"user_id", "platform"})
)
public class PlatformAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Platform platform;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "profile_url", length = 500)
    private String profileUrl;

    @Column(nullable = false)
    private Boolean verified = false;

    @Column(name = "connected_at", nullable = false, updatable = false)
    private LocalDateTime connectedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 20)
    private SyncStatus syncStatus = SyncStatus.NEVER_SYNCED;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "last_success_at")
    private LocalDateTime lastSuccessAt;

    @Column(name = "last_failure_at")
    private LocalDateTime lastFailureAt;

    @Column(name = "last_sync_error_category", length = 40)
    private String lastSyncErrorCategory;

    @Column(name = "last_sync_error_message", length = 160)
    private String lastSyncErrorMessage;

    public PlatformAccount() {
    }

    public PlatformAccount(User user, Platform platform, String username, String profileUrl) {
        this.user = user;
        this.platform = platform;
        this.username = username;
        this.profileUrl = profileUrl;
        this.verified = false;
    }

    @PrePersist
    protected void onCreate() {
        this.connectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.verified == null) {
            this.verified = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(LocalDateTime connectedAt) {
        this.connectedAt = connectedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public SyncStatus getSyncStatus() { return syncStatus; }
    public void setSyncStatus(SyncStatus syncStatus) { this.syncStatus = syncStatus; }
    public LocalDateTime getLastAttemptAt() { return lastAttemptAt; }
    public void setLastAttemptAt(LocalDateTime lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }
    public LocalDateTime getLastSuccessAt() { return lastSuccessAt; }
    public void setLastSuccessAt(LocalDateTime lastSuccessAt) { this.lastSuccessAt = lastSuccessAt; }
    public LocalDateTime getLastFailureAt() { return lastFailureAt; }
    public void setLastFailureAt(LocalDateTime lastFailureAt) { this.lastFailureAt = lastFailureAt; }
    public String getLastSyncErrorCategory() { return lastSyncErrorCategory; }
    public void setLastSyncErrorCategory(String value) { this.lastSyncErrorCategory = value; }
    public String getLastSyncErrorMessage() { return lastSyncErrorMessage; }
    public void setLastSyncErrorMessage(String value) { this.lastSyncErrorMessage = value; }
}
