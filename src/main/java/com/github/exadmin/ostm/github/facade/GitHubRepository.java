package com.github.exadmin.ostm.github.facade;

public class GitHubRepository {
    private final String id;
    private final String name;
    private final String url;
    private final String cloneUrl;

    public GitHubRepository(String id, String name, String url, String cloneUrl) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.cloneUrl = cloneUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getCloneUrl() {
        return cloneUrl;
    }
}
