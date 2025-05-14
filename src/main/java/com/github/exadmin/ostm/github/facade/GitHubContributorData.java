package com.github.exadmin.ostm.github.facade;

public class GitHubContributorData {
    private String repositoryName;
    private String repositoryOwner;
    private String contributorId;
    private String login;
    private int contributionsCount;

    public GitHubContributorData(String contributorId, String login) {
        this.contributorId = contributorId;
        this.login = login;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getRepositoryOwner() {
        return repositoryOwner;
    }

    public String getContributorId() {
        return contributorId;
    }

    public String getLogin() {
        return login;
    }

    public int getContributionsCount() {
        return contributionsCount;
    }

    void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    void setRepositoryOwner(String repositoryOwner) {
        this.repositoryOwner = repositoryOwner;
    }

    void setContributorId(String contributorId) {
        this.contributorId = contributorId;
    }

    void setLogin(String login) {
        this.login = login;
    }

    void setContributionsCount(int contributions) {
        this.contributionsCount = contributions;
    }
}
