package com.kodbtw.dto.leaderboard;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Paginated leaderboard response.
 */
public class LeaderboardPageResponse {

    private final int page;
    private final int size;
    private final long total;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime lastSyncedAt;

    private final List<LeaderboardEntryDto> entries;

    /** Global rank of the authenticated user. Null if not synced or no real data. */
    private final Long myRank;

    public LeaderboardPageResponse(int page, int size, long total,
                                   LocalDateTime lastSyncedAt,
                                   List<LeaderboardEntryDto> entries,
                                   Long myRank) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.lastSyncedAt = lastSyncedAt;
        this.entries = entries;
        this.myRank = myRank;
    }

    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotal() { return total; }
    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public List<LeaderboardEntryDto> getEntries() { return entries; }
    public Long getMyRank() { return myRank; }
}
