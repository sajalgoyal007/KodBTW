package com.kodbtw.dto.publicprofile;

public class PublicSocialLinksDto {

    private String githubUrl;
    private String linkedinUrl;
    private String portfolioUrl;

    public PublicSocialLinksDto() {
    }

    public PublicSocialLinksDto(String githubUrl, String linkedinUrl, String portfolioUrl) {
        this.githubUrl = githubUrl;
        this.linkedinUrl = linkedinUrl;
        this.portfolioUrl = portfolioUrl;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getPortfolioUrl() {
        return portfolioUrl;
    }

    public void setPortfolioUrl(String portfolioUrl) {
        this.portfolioUrl = portfolioUrl;
    }
}
