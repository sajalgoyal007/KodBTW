package com.kodbtw.dto.publicprofile;

public class PublicOverviewDto {

    private int totalProblemsSolved;
    private int easySolved;
    private int mediumSolved;
    private int hardSolved;
    private int contestsParticipated;
    private Integer bestRating;
    private String bestRatingPlatform;
    private Integer currentStreak;
    private Integer longestStreak;

    public PublicOverviewDto() {
    }

    public PublicOverviewDto(int totalProblemsSolved, int easySolved, int mediumSolved, int hardSolved,
                             int contestsParticipated, Integer bestRating, String bestRatingPlatform,
                             Integer currentStreak, Integer longestStreak) {
        this.totalProblemsSolved = totalProblemsSolved;
        this.easySolved = easySolved;
        this.mediumSolved = mediumSolved;
        this.hardSolved = hardSolved;
        this.contestsParticipated = contestsParticipated;
        this.bestRating = bestRating;
        this.bestRatingPlatform = bestRatingPlatform;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
    }

    public int getTotalProblemsSolved() { return totalProblemsSolved; }
    public void setTotalProblemsSolved(int totalProblemsSolved) { this.totalProblemsSolved = totalProblemsSolved; }

    public int getEasySolved() { return easySolved; }
    public void setEasySolved(int easySolved) { this.easySolved = easySolved; }

    public int getMediumSolved() { return mediumSolved; }
    public void setMediumSolved(int mediumSolved) { this.mediumSolved = mediumSolved; }

    public int getHardSolved() { return hardSolved; }
    public void setHardSolved(int hardSolved) { this.hardSolved = hardSolved; }

    public int getContestsParticipated() { return contestsParticipated; }
    public void setContestsParticipated(int contestsParticipated) { this.contestsParticipated = contestsParticipated; }

    public Integer getBestRating() { return bestRating; }
    public void setBestRating(Integer bestRating) { this.bestRating = bestRating; }

    public String getBestRatingPlatform() { return bestRatingPlatform; }
    public void setBestRatingPlatform(String bestRatingPlatform) { this.bestRatingPlatform = bestRatingPlatform; }

    public Integer getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(Integer currentStreak) { this.currentStreak = currentStreak; }

    public Integer getLongestStreak() { return longestStreak; }
    public void setLongestStreak(Integer longestStreak) { this.longestStreak = longestStreak; }
}
