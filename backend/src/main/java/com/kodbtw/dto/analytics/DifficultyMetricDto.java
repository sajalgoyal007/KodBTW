package com.kodbtw.dto.analytics;

public class DifficultyMetricDto {

    private Integer count;
    private Double percentage;

    public DifficultyMetricDto() {
    }

    public DifficultyMetricDto(Integer count, Double percentage) {
        this.count = count;
        this.percentage = percentage;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}
