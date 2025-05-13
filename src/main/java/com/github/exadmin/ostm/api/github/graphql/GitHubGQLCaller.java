package com.github.exadmin.ostm.api.github.graphql;

import com.github.exadmin.ostm.api.collector.ApplicationContext;
import com.github.exadmin.ostm.api.github.cache.NewCacheManager;
import com.github.exadmin.ostm.api.github.rest.GitHubResponse;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;

public class GitHubGQLCaller {
    private static final String GITHUB_GQL_END_POINT = "https://api.github.com/graphql";
    private static final Logger log = LoggerFactory.getLogger(GitHubGQLCaller.class);
    private static final Long CACHE_TTL_IN_SECONDS = 8L/*hours*/ * 60L/*minutes*/ * 60L/*seconds*/;

    private NewCacheManager cacheManager;
    private String authToken;

    public GitHubGQLCaller(ApplicationContext applicationContext) {
        cacheManager = new NewCacheManager(applicationContext.getCacheDir());
        authToken = applicationContext.getGitHubToken();
    }

    public GitHubResponse doCall(String query, long cacheTTLInSeconds) {
        // check cache first
        String responseBody = cacheManager.getFromCache(GITHUB_GQL_END_POINT, query);
        if (responseBody != null) {
            return new GitHubResponse(200, responseBody);
        }

        // do real request in case nothing was found in the cache
        Map.Entry<Integer, String> result = doCallNoCache(query);
        if (result == null) return emptyResponse();

        if (result.getKey() == 200) {
            cacheManager.putToCache(GITHUB_GQL_END_POINT, query, result.getValue(), CACHE_TTL_IN_SECONDS);
        }

        return new GitHubResponse(result.getKey(), result.getValue());
    }

    /**
     * Performs real call to end-point and returns HTTP CODE + String response body
     * @param query String graph-QL query
     * @return Map.Entry where key is http-code and value is response body
     */
    private Map.Entry<Integer, String> doCallNoCache(String query) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // HttpPost httpPost = new HttpPost(url);
            HttpPost httpRequest = new HttpPost(GITHUB_GQL_END_POINT);

            httpRequest.setHeader("Accept", "application/vnd.github+json");
            httpRequest.setHeader("Authorization", "Bearer " + authToken);
            httpRequest.setHeader("X-GitHub-Api-Version", "2022-11-28");
            httpRequest.setEntity(new StringEntity(query));

            try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
                HttpEntity responseEntity = response.getEntity();
                String responseStr = EntityUtils.toString(responseEntity);
                EntityUtils.consume(responseEntity);

                return new AbstractMap.SimpleEntry<>(response.getCode(), responseStr);
            }

        } catch (IOException ex) {
            log.error("Error while calling URL '{}'", GITHUB_GQL_END_POINT, ex);
        } catch (ParseException pe) {
            log.error("Error while parsing response", pe);
        }

        return null;
    }

    private static GitHubResponse emptyResponse() {
        return new GitHubResponse(0, "[]");
    }
}
