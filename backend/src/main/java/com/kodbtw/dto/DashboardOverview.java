package com.kodbtw.dto;

public class DashboardOverview {

    private Integer totalProblemsSolved;
    private Integer easySolved;
    private Integer mediumSolved;
    private Integer hardSolved;
    private Integer contestsParticipated;
    private Integer currentStreak;
    private Integer longestStreak;
    private int connectedPlatformsCount;

    public DashboardOverview() {
    }

    public DashboardOverview(Integer totalProblemsSolved, Integer easySolved, Integer mediumSolved,
                             Integer hardSolved, Integer contestsParticipated,
                             Integer currentStreak, Integer longestStreak,
                             int connectedPlatformsCount) {
        this.totalProblemsSolved = totalProblemsSolved;
        this.easySolved = easySolved;
        this.mediumSolved = mediumSolved;
        this.hardSolved = hardSolved;
        this.contestsParticipated = contestsParticipated;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.connectedPlatformsCount = connectedPlatformsCount;
    }

    public Integer getTotalProblemsSolved() {
        return totalProblemsSolved;
    }

    public void setTotalProblemsSolved(Integer totalProblemsSolved) {
        this.totalProblemsSolved = totalProblemsSolved;
    }

    public Integer getEasySolved() {
        return easySolved;
    }

    public void setEasySolved(Integer easySolved) {
        this.easySolved = easySolved;
    }

    public Integer getMediumSolved() {
        return mediumSolved;
    }

    public void setMediumSolved(Integer mediumSolved) {
        this.mediumSolved = mediumSolved;
    }

    public Integer getHardSolved() {
        return hardSolved;
    }

    public void setHardSolved(Integer hardSolved) {
        this.hardSolved = hardSolved;
    }

    public Integer getContestsParticipated() {
        return contestsParticipated;
    }

    public void setContestsParticipated(Integer contestsParticipated) {
        this.contestsParticipated = contestsParticipated;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(Integer longestStreak) {
        this.longestStreak = longestStreak;
    }

    public int getConnectedPlatformsCount() {
        return connectedPlatformsCount;
    }

    public void setConnectedPlatformsCount(int connectedPlatformsCount) {
        this.connectedPlatformsCount = connectedPlatformsCount;
    }
}
