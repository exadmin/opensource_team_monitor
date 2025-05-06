package com.github.exadmin.ostm.api.github.rest;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.exadmin.ostm.api.collector.ApplicationContext;
import com.github.exadmin.ostm.api.github.cache.CacheManager;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
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

    private final ApplicationContext applicationContext;
    private boolean autoPaging = false;
    private int itemsPerPage = 50;

    public GitHubRESTApiCaller(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setAutoPaging(boolean autoPaging) {
        this.autoPaging = autoPaging;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public GitHubResponse doGet(String httpGetQueryUrl, long ttlForCacheInSeconds) {
        List<Map<String, Object>> cachedData = CacheManager.getFromCache(httpGetQueryUrl, applicationContext);
        if (cachedData != null) {
            return new GitHubResponse(200, cachedData); // todo: check http-code
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // HttpPost httpPost = new HttpPost(url);
            HttpUriRequestBase httpRequest = new HttpGet(httpGetQueryUrl);

            httpRequest.setHeader("Accept", "application/vnd.github+json");
            httpRequest.setHeader("Authorization", "Bearer " + applicationContext.getGitHubToken());
            httpRequest.setHeader("X-GitHub-Api-Version", "2022-11-28");

            try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
                int httpCode = response.getCode();
                if (httpCode == 200) {
                    HttpEntity responseEntity = response.getEntity();
                    String str = EntityUtils.toString(responseEntity);
                    log.debug("Response = '{}'", str);
                    List<Map<String, Object>> dataMap = OBJECT_MAPPER.readValue(str, type);

                    EntityUtils.consume(responseEntity);

                    if (ttlForCacheInSeconds > 0) {
                        CacheManager.putToCache(httpGetQueryUrl, dataMap, ttlForCacheInSeconds, applicationContext);
                    }
                    return new GitHubResponse(httpCode, dataMap);
                }

                log.warn("Unexpected answer: http-code = {}", httpCode);
                return new GitHubResponse(httpCode, null);
            }

        } catch (IOException ex) {
            log.error("Error while calling URL '{}'", httpGetQueryUrl, ex);
        } catch (ParseException pe) {
            log.error("Error while parsing response", pe);
        }

        return new GitHubResponse(0, null);
    }

    public GitHubResponse doGetWithAutoPaging(String httpGetQueryUrl, long ttlForCacheInSeconds) {
        GitHubResponse ghResponse = new GitHubResponse(0, null);

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
            return new GitHubResponse(0, null);
        }
    }
}
