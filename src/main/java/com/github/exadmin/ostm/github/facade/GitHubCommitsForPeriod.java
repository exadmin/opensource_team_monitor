package com.github.exadmin.ostm.github.facade;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class GitHubCommitsForPeriod {
    private String login;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Map<String, Integer> commitsMap;
    private Map<String, Integer> prsMap;
    private Map<String, Integer> issuesMap;

    GitHubCommitsForPeriod(String login) {
        this.login = login;
        this.commitsMap = new HashMap<>();
        this.prsMap = new HashMap<>();
        this.issuesMap = new HashMap<>();
    }

    public String getLogin() {
        return login;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public Map<String, Integer> getCommitsMap() {
        return commitsMap;
    }

    public Map<String, Integer> getPrsMap() {
        return prsMap;
    }

    public Map<String, Integer> getIssuesMap() {
        return issuesMap;
    }

    public void addCommitsCounter(String repoUrl, Integer commitsCount) {
        commitsMap.put(repoUrl, commitsCount);
    }

    public void addPRsCounter(String repoUrl, Integer prsCount) {
        prsMap.put(repoUrl, prsCount);
    }

    public void addIssuesCounter(String repoUrl, Integer issuesCount) {
        issuesMap.put(repoUrl, issuesCount);
    }
}
