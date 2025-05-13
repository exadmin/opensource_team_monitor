package com.github.exadmin.ostm.api.github.rest;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.exadmin.ostm.api.collector.ApplicationContext;
import com.github.exadmin.ostm.api.github.cache.NewCacheManager;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GitHubRESTApiCaller {
    private static final Logger log = LoggerFactory.getLogger(GitHubRESTApiCaller.class);
    private static final TypeReference<List<Map<String, Object>>> type = new TypeReference<>() {};
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature());
    }

    private int itemsPerPage = 50;

    private NewCacheManager cacheManager;
    private String authToken;

    public GitHubRESTApiCaller(ApplicationContext applicationContext) {
        this.cacheManager = new NewCacheManager(applicationContext.getCacheDir());
        this.authToken = applicationContext.getGitHubToken();
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public GitHubResponse doGet(String httpGetQueryUrl, long ttlForCacheInSeconds) {
        log.trace("GET {}", httpGetQueryUrl);

        String responseBody = cacheManager.getFromCache(httpGetQueryUrl, "");
        if (responseBody != null) {
            log.trace("Response is returned from cache");
            return new GitHubResponse(200, responseBody);
        }

        log.trace("No actual cache is found");
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // HttpPost httpPost = new HttpPost(url);
            HttpUriRequestBase httpRequest = new HttpGet(httpGetQueryUrl);

            httpRequest.setHeader("Accept", "application/vnd.github+json");
            httpRequest.setHeader("Authorization", "Bearer " + authToken);
            httpRequest.setHeader("X-GitHub-Api-Version", "2022-11-28");

            int httpCode = 0;
            String responseBodyStr = "[]";
            try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
                httpCode = response.getCode();


                if (httpCode != 204) {
                    HttpEntity responseEntity = response.getEntity();
                    responseBodyStr = EntityUtils.toString(responseEntity);
                    EntityUtils.consume(responseEntity);
                }

                if (httpCode == 200) {
                    cacheManager.putToCache(httpGetQueryUrl, "", responseBodyStr, ttlForCacheInSeconds);
                }

                return new GitHubResponse(httpCode, responseBodyStr);
            } catch (Exception ex) {
                log.error("Error while performing GET request to {}, HTTP Code = {}", httpGetQueryUrl, httpCode, ex);
                return emptyResponse();
            }

        } catch (IOException ex) {
            log.error("Error while calling URL '{}'", httpGetQueryUrl, ex);
        }

        return emptyResponse();
    }

    public GitHubResponse doGetWithAutoPaging(String httpGetQueryUrl, long ttlForCacheInSeconds) {
        GitHubResponse ghResponse = emptyResponse();

        try {
            URIBuilder uriBuilder = new URIBuilder(httpGetQueryUrl);
            uriBuilder.setParameter("per_page", "" + itemsPerPage);

            for (int pageNumber = 1; pageNumber < 1024; pageNumber++) {
                uriBuilder.setParameter("page", "" + pageNumber);

                GitHubResponse ghTmpResponse = doGet(uriBuilder.build().toString(), ttlForCacheInSeconds);
                ghResponse.setHttpCode(ghTmpResponse.getHttpCode());

                List<Map<String, Object>> tmpList = ghTmpResponse.getDataMap();
                if (tmpList == null || tmpList.isEmpty()) {
                    break;
                } else {
                    List<Map<String, Object>> list = ghResponse.getDataMap();
                    if (list == null) {
                        list = new ArrayList<>();
                        ghResponse.setDataMap(list);
                    }

                    list.addAll(tmpList);
                }
            }

            return ghResponse;
        } catch (Exception ex) {
            log.error("Error while processing URL = '{}' in auto-paging mode", httpGetQueryUrl, ex);
            return emptyResponse();
        }
    }

    private static GitHubResponse emptyResponse() {
        return new GitHubResponse(0, "[]");
    }
}
