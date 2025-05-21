package com.github.exadmin.ostm.github.api;

import org.apache.hc.core5.http.Method;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class GitHubHttpRequestBuilderREST extends HttpRequestBuilder {
    protected GitHubRequest request;

    GitHubHttpRequestBuilderREST(String token) {
        this.request = new GitHubRequest();
        this.request.method   = Method.GET;
        this.request.bodyText = "";
        this.request.token    = token;
    }

    protected GitHubHttpRequestBuilderREST getThis() {
        return this;
    }

    public GitHubHttpRequestBuilderREST fetchPages(int itemsPerPage, int fromPage, int toPage) {
        this.request.fromPage = fromPage;
        this.request.toPage   = toPage;
        this.request.itemsPerPage = itemsPerPage;

        return getThis();
    }

    public GitHubHttpRequestBuilderREST toURL(String url) {
        this.request.url = url;

        return getThis();
    }

    @Override
    public GitHubRequest build() {
        if (isEmpty(request.url)) throw new IllegalStateException("URL is not set");
        if (isEmpty(request.token)) throw new IllegalStateException("Authentication token is not set");
        if (request.itemsPerPage < 1) throw new IllegalStateException("Incorrect value for itemsPerPage parameter");
        if (request.toPage < request.fromPage) throw new IllegalStateException("toPage is less than fromPage");

        return request;
    }
}
