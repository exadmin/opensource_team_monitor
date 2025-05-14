package com.github.exadmin.ostm.github.api;

public abstract class GitHubRequestBuilder {
    private static String authToken;

    GitHubRequestBuilder() {
    }

    public static void setAuthenticationToken(String token) {
        authToken = token;
    }

    public static GitHubRequestBuilderGQL graphQL() {
        ensureTokenIsSet();
        return new GitHubRequestBuilderGQL(authToken);
    }

    public static GitHubRequestBuilderREST rest() {
        ensureTokenIsSet();
        return new GitHubRequestBuilderREST(authToken);
    }

    private static void ensureTokenIsSet() {
        if (authToken == null) throw new IllegalStateException("Set authentication token first!");
    }

    public abstract GitHubRequest build();
}
