package com.kodbtw.dto.publicprofile;

public class PublicOverviewDto {

    private Integer totalProblemsSolved;
    private Integer easySolved;
    private Integer mediumSolved;
    private Integer hardSolved;
    private Integer contestsParticipated;
    private Integer bestRating;
    private String bestRatingPlatform;
    private Integer currentStreak;
    private Integer longestStreak;

    public PublicOverviewDto() {
    }

    public PublicOverviewDto(Integer totalProblemsSolved, Integer easySolved, Integer mediumSolved, Integer hardSolved,
                             Integer contestsParticipated, Integer bestRating, String bestRatingPlatform,
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

    public Integer getTotalProblemsSolved() { return totalProblemsSolved; }
    public void setTotalProblemsSolved(Integer totalProblemsSolved) { this.totalProblemsSolved = totalProblemsSolved; }

    public Integer getEasySolved() { return easySolved; }
    public void setEasySolved(Integer easySolved) { this.easySolved = easySolved; }

    public Integer getMediumSolved() { return mediumSolved; }
    public void setMediumSolved(Integer mediumSolved) { this.mediumSolved = mediumSolved; }

    public Integer getHardSolved() { return hardSolved; }
    public void setHardSolved(Integer hardSolved) { this.hardSolved = hardSolved; }

    public Integer getContestsParticipated() { return contestsParticipated; }
    public void setContestsParticipated(Integer contestsParticipated) { this.contestsParticipated = contestsParticipated; }

    public Integer getBestRating() { return bestRating; }
    public void setBestRating(Integer bestRating) { this.bestRating = bestRating; }

    public String getBestRatingPlatform() { return bestRatingPlatform; }
    public void setBestRatingPlatform(String bestRatingPlatform) { this.bestRatingPlatform = bestRatingPlatform; }

    public Integer getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(Integer currentStreak) { this.currentStreak = currentStreak; }

    public Integer getLongestStreak() { return longestStreak; }
    public void setLongestStreak(Integer longestStreak) { this.longestStreak = longestStreak; }
}
