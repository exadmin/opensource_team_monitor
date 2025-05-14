package com.github.exadmin.ostm.github.api;

import com.github.exadmin.ostm.github.cache.NewCacheManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GitHubRequestExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(GitHubRequestExecutor.class);
    private static final int CACHE_TTL_IN_SECONDS = 24/*hours*/ * 60/*minutes*/ * 60/*seconds*/;
    private final NewCacheManager cacheManager = new NewCacheManager();

    GitHubRequestExecutor() {
    }

    GitHubResponse execute(GitHubRequest request) {
        // if multi-paging request
        if (request.method.equals(Method.GET) && request.fromPage > 0 && request.toPage > request.fromPage) {
            try {
                return executeWithPaging(request);
            } catch (URISyntaxException ex) {
                LOG.error("Error while performing GET request with paging options, uri = {}", request.url, ex);
                throw new IllegalStateException(ex);
            }

        } else {
            return executeNoPaging(request);
        }
    }

    private GitHubResponse executeWithPaging(GitHubRequest request) throws URISyntaxException {
        List<Map<String, Object>> commonDataResult = new ArrayList<>();
        int httpResponseCode = 0;

        URIBuilder uriBuilder = new URIBuilder(request.url);
        uriBuilder.setParameter("per_page", "" + request.itemsPerPage);

        int currentPage = request.fromPage;
        while (currentPage <= request.toPage) {
            uriBuilder.setParameter("page", "" + currentPage);

            GitHubRequest pageRequest = request.cloneMe();
            pageRequest.url = uriBuilder.build().toString();

            GitHubResponse pageResponse = executeNoPaging(pageRequest);
            List<Map<String, Object>> pageData = pageResponse.getDataMap();

            if (pageData == null || pageData.isEmpty()) break;

            commonDataResult.addAll(pageData);
            httpResponseCode = pageResponse.getHttpCode();

            currentPage++;
        }

        GitHubResponse response = new GitHubResponse(httpResponseCode, "[]");
        response.setDataMap(commonDataResult);
        return response;
    }

    private GitHubResponse executeNoPaging(GitHubRequest request) {
        // check cache first
        String responseBody = cacheManager.getFromCache(request.url, request.bodyText);
        if (responseBody != null) {
            return new GitHubResponse(200, responseBody);
        }

        // do real request in case nothing was found in the cache
        // todo: add paging
        Map.Entry<Integer, String> result = executeNoCache(request);
        if (result == null) return emptyResponse();

        if (result.getKey() == 200) {
            cacheManager.putToCache(request.url, request.bodyText, result.getValue(), CACHE_TTL_IN_SECONDS);
        }

        return new GitHubResponse(result.getKey(), result.getValue());
    }

    private static GitHubResponse emptyResponse() {
        return new GitHubResponse(0, "[]");
    }

    private Map.Entry<Integer, String> executeNoCache(GitHubRequest request) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpUriRequestBase httpRequest = request.method == Method.GET ? new HttpGet(request.url) : new HttpPost(request.url);

            httpRequest.setHeader("Accept", "application/vnd.github+json");
            httpRequest.setHeader("X-GitHub-Api-Version", "2022-11-28");
            if (StringUtils.isNotEmpty(request.token)) httpRequest.setHeader("Authorization", "Bearer " + request.token);
            if (StringUtils.isNotEmpty(request.bodyText)) {
                httpRequest.setEntity(new StringEntity(request.bodyText));
            }

            // todo: execute(request) to replace with execute(request, handler)
            // see: https://hc.apache.org/httpcomponents-client-5.4.x/current/httpclient5/apidocs/org/apache/hc/client5/http/classic/HttpClient.html#execute-org.apache.hc.core5.http.ClassicHttpRequest-org.apache.hc.core5.http.io.HttpClientResponseHandler-
            try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
                int responseCode = response.getCode();
                String responseBody = "[]";

                // if NO-CONTENT response
                if (responseCode != HttpStatus.SC_NO_CONTENT) {
                    HttpEntity responseEntity = response.getEntity();
                    responseBody = EntityUtils.toString(responseEntity);

                    EntityUtils.consume(responseEntity);
                }

                return new AbstractMap.SimpleEntry<>(response.getCode(), responseBody);
            }
        } catch (IOException ex) {
            LOG.error("Error while calling URL '{}'", request.url, ex);
        } catch (ParseException pe) {
            LOG.error("Error while parsing response", pe);
        }

        return null;
    }
}
