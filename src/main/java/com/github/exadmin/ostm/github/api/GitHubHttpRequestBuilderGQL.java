package com.github.exadmin.ostm.github.api;



import org.apache.commons.text.StringEscapeUtils;
import org.apache.hc.core5.http.Method;

import static org.apache.commons.lang3.StringUtils.isEmpty;

// play at: https://docs.github.com/ru/graphql/overview/explorer
public class GitHubHttpRequestBuilderGQL extends HttpRequestBuilder {
    protected static final String GRAPH_QL_END_POINT = "https://api.github.com/graphql";

    protected GitHubRequest request;

    GitHubHttpRequestBuilderGQL(String token) {
        this.request = new GitHubRequest();
        this.request.method = Method.POST;
        this.request.url = GRAPH_QL_END_POINT;
        this.request.token = token;
    }

    protected GitHubHttpRequestBuilderGQL getThis() {
        return this;
    }

    public GitHubHttpRequestBuilderGQL useQuery(String gqlQuery) {
        this.request.bodyText = gqlQuery;

        return getThis();
    }

    @Override
    public GitHubRequest build() {
        // wrap GQL query into JSON model
        String queryText = StringEscapeUtils.escapeJson(request.bodyText);
        request.bodyText = "{\"query\":\"" + queryText + "\"}";

        if (isEmpty(request.url)) throw new IllegalStateException("URL is not set");
        if (isEmpty(request.token)) throw new IllegalStateException("Authentication token is not set");
        if (isEmpty(request.bodyText)) throw new IllegalStateException("GraphQL query is not set");

        return request;
    }
}
