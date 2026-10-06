package com.kodbtw.dto;

import java.util.ArrayList;
import java.util.List;

public class DashboardStatsResponse {

    private DashboardOverview overview;
    private List<PlatformStats> platforms;

    public DashboardStatsResponse() {
        this.overview = new DashboardOverview();
        this.platforms = new ArrayList<>();
    }

    public DashboardStatsResponse(DashboardOverview overview, List<PlatformStats> platforms) {
        this.overview = overview;
        this.platforms = platforms != null ? platforms : new ArrayList<>();
    }

    public DashboardOverview getOverview() {
        return overview;
    }

    public void setOverview(DashboardOverview overview) {
        this.overview = overview;
    }

    public List<PlatformStats> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<PlatformStats> platforms) {
        this.platforms = platforms != null ? platforms : new ArrayList<>();
    }
}
