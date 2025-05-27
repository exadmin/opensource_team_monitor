package com.github.exadmin.ostm.app;

// todo: refactor - remove this singleton (like)
public class AppSettings {
    private static String gitHubAuthenticationToken;

    public static String getGitHubAuthenticationToken() {
        return gitHubAuthenticationToken;
    }

    public static void setGitHubAuthenticationToken(String gitHubAuthenticationToken) {
        AppSettings.gitHubAuthenticationToken = gitHubAuthenticationToken;
    }
}
