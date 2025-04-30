package com.github.exadmin.ostm.api.model.collector;

import java.nio.file.Path;

public class ApplicationContext {
    private String gitHubToken;
    private Path cacheDir;

    public String getGitHubToken() {
        return gitHubToken;
    }

    public void setGitHubToken(String gitHubToken) {
        this.gitHubToken = gitHubToken;
    }

    public Path getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(Path cacheDir) {
        this.cacheDir = cacheDir;
    }
}
