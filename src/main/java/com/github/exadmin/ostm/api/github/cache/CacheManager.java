package com.github.exadmin.ostm.api.github.cache;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.exadmin.ostm.api.model.collector.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class CacheManager {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(CacheManager.class);
    private static final TypeReference<CacheContainer> TYPE_REFERENCE = new TypeReference<CacheContainer>() {};

    public static List<Map<String, Object>> getFromCache(String httpGetQueryUrl, ApplicationContext applicationContext) {
        try {
            log.trace("Attepmt to load data from cache for the url = '{}'", httpGetQueryUrl);
            Path cacheDir = applicationContext.getCacheDir();
            Path filePath = Paths.get(cacheDir.toString(), getFileName(httpGetQueryUrl));
            if (!filePath.toFile().exists() || !filePath.toFile().isFile()) {
                log.trace("No cache file is found for url = '{}'", httpGetQueryUrl);
                return null;
            }

            CacheContainer cacheContainer = OBJECT_MAPPER.readValue(filePath.toFile(), CacheContainer.class);
            log.trace("Cache container is loaded for url = '{}'", httpGetQueryUrl);

            long currentTime = System.currentTimeMillis();
            long cachedDataTimeTillTime = cacheContainer.getCreatedWhen() + cacheContainer.getTtlInSeconds() * 1000;
            if (currentTime < cachedDataTimeTillTime) {
                log.trace("Return actual data from cache for url = '{}'", httpGetQueryUrl);
                return cacheContainer.getDataMapList();
            }

            log.trace("Cached data is not actual. Current time = {}, cached data was actual till {}, http url = '{}'", currentTime, cachedDataTimeTillTime, httpGetQueryUrl);
            return null;
        } catch (Exception ex) {
            log.warn("Error while reading cached content for URL = '{}'", httpGetQueryUrl, ex);
            return null;
        }
    }

    public static void putToCache(String httpGetQueryUrl, List<Map<String, Object>> dataMapList, long ttlSeconds, ApplicationContext applicationContext) {
        CacheContainer cacheContainer = new CacheContainer();
        cacheContainer.setCreatedWhen(System.currentTimeMillis());
        cacheContainer.setHttpGetQueryUrl(httpGetQueryUrl);
        cacheContainer.setTtlInSeconds(ttlSeconds);
        cacheContainer.setDataMapList(dataMapList);

        Path cacheDir = applicationContext.getCacheDir();
        Path filePath = Paths.get(cacheDir.toString(), getFileName(httpGetQueryUrl));
        File file = filePath.toFile();
        if (file.isFile() && file.exists()) {
            file.delete();
        }

        try {
            OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            OBJECT_MAPPER.writeValue(filePath.toFile(), cacheContainer);
        } catch (Exception ex) {
            log.warn("Error while saving data to cache file = '{}', url = '{}'", file, httpGetQueryUrl);
        }
    }

    private static String getFileName(String httpGetQueryUrl) {
        return httpGetQueryUrl.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }
}
