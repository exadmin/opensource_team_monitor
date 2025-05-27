package com.github.exadmin.ostm.github.api;

import com.github.exadmin.ostm.app.AppSettings;

public abstract class HttpRequestBuilder {
    HttpRequestBuilder() {
    }

    public static GitHubHttpRequestBuilderGQL gitHubGraphQLCall() {
        ensureTokenIsSet();
        return new GitHubHttpRequestBuilderGQL(AppSettings.getGitHubAuthenticationToken());
    }

    public static GitHubHttpRequestBuilderREST gitHubRESTCall() {
        ensureTokenIsSet();
        return new GitHubHttpRequestBuilderREST(AppSettings.getGitHubAuthenticationToken());
    }

    public static GitHubHttpRequestBuilderREST genericRESTCall() {
        return new GitHubHttpRequestBuilderREST(TempConstants.NO_TOKEN); // todo: refactor class name
    }

    private static void ensureTokenIsSet() {
        if (AppSettings.getGitHubAuthenticationToken() == null)
            throw new IllegalStateException("Set authentication token first!");
    }

    public abstract GitHubRequest build();
}
