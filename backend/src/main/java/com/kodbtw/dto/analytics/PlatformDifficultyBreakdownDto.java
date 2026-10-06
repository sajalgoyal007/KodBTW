package com.kodbtw.dto.analytics;

import com.kodbtw.entity.Platform;

public class PlatformDifficultyBreakdownDto {

    private Platform platform;
    private Integer easy;
    private Integer medium;
    private Integer hard;
    private Integer total;

    public PlatformDifficultyBreakdownDto() {
    }

    public PlatformDifficultyBreakdownDto(Platform platform, Integer easy, Integer medium, Integer hard, Integer total) {
        this.platform = platform;
        this.easy = easy;
        this.medium = medium;
        this.hard = hard;
        this.total = total;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public Integer getEasy() {
        return easy;
    }

    public void setEasy(Integer easy) {
        this.easy = easy;
    }

    public Integer getMedium() {
        return medium;
    }

    public void setMedium(Integer medium) {
        this.medium = medium;
    }

    public Integer getHard() {
        return hard;
    }

    public void setHard(Integer hard) {
        this.hard = hard;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
