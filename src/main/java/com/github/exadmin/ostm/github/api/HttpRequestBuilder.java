package com.github.exadmin.ostm.github.api;

public abstract class HttpRequestBuilder {
    private static String authToken;

    HttpRequestBuilder() {
    }

    public static void setAuthenticationToken(String token) {
        authToken = token;
    }

    public static GitHubHttpRequestBuilderGQL gitHubGraphQLCall() {
        ensureTokenIsSet();
        return new GitHubHttpRequestBuilderGQL(authToken);
    }

    public static GitHubHttpRequestBuilderREST gitHubRESTCall() {
        ensureTokenIsSet();
        return new GitHubHttpRequestBuilderREST(authToken);
    }

    public static GitHubHttpRequestBuilderREST genericRESTCall() {
        return new GitHubHttpRequestBuilderREST(TempConstants.NO_TOKEN); // todo: refactor class name
    }

    private static void ensureTokenIsSet() {
        if (authToken == null) throw new IllegalStateException("Set authentication token first!");
    }

    public abstract GitHubRequest build();
}
