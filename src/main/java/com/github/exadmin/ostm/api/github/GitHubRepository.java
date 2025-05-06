package com.github.exadmin.ostm.api.github;

public class GitHubRepository {
    private String id;
    private String name;
    private String url;
    private String cloneUrl;

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
