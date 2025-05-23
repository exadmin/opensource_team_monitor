package com.github.exadmin.ostm.github.cache;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.exadmin.ostm.utils.MiscUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NewCacheManager {
    private static String cacheDirectoryPath = null;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(NewCacheManager.class);

    private final Path cacheDirectory;

    public static void setCacheDirectoryPath(String cacheDirectoryPath) {
        NewCacheManager.cacheDirectoryPath = cacheDirectoryPath;
    }

    public NewCacheManager() {
        this.cacheDirectory = Paths.get(cacheDirectoryPath);
        this.cacheDirectory.toFile().mkdirs();
    }

    public String getFromCache(String fullUrl, String requestBody) {
        File cacheFile = getFileNameByContent(fullUrl, requestBody);
        if (!cacheFile.exists() && !cacheFile.isFile()) {
            return null; // no cache file is found
        }

        try {
            CachedEntity entity = OBJECT_MAPPER.readValue(cacheFile, CachedEntity.class);

            // check if cache entity is actual
            long currentTime = System.currentTimeMillis();
            long entityTime  = entity.getValidTill();
            if (currentTime < entityTime) {
                log.debug("Return result from cache {}", cacheFile);
                return entity.getResponseBody();
            }

            // just clean up obsolete cache file
            deleteCachedEntity(cacheFile);
            return null;
        } catch (Exception ex) {
            log.error("Error while loading cache file {}", cacheFile, ex);
            return null;
        }
    }

    public void putToCache(String fullUrl, String requestBody, String responseBody, long cacheTTLInSeconds) {
        File cacheFile = getFileNameByContent(fullUrl, requestBody);
        long currentTime = System.currentTimeMillis();

        log.debug("Store response to the cache file {}", cacheFile);

        CachedEntity entity = new CachedEntity();
        entity.setUrl(fullUrl);
        entity.setRequestBody(requestBody);
        entity.setResponseBody(responseBody);
        entity.setCreatedWhen(currentTime);
        entity.setValidTill(currentTime + cacheTTLInSeconds * 1000);

        try {
            OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            OBJECT_MAPPER.writeValue(cacheFile, entity);
        } catch (Exception ex) {
            log.warn("Error while saving data to cache file = '{}', url = '{}'", cacheFile, fullUrl);
        }
    }

    /**
     * Utility methods
     */
    private static String getFileNameBy(String httpGetQueryUrl) {
        String fileName = httpGetQueryUrl.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
        int length = Math.min(fileName.length(), 128);
        return fileName.substring(0, length);
    }

    private static synchronized void deleteCachedEntity(File file) {
        if (file.exists()) {
            boolean result = file.delete();
            log.debug("Cached entity file {} was {}deleted", file, result ? "" : "not ");
        }
    }

    private File getFileNameByContent(String fullUrl, String requestBody) {
        // generate possible file name for the request
        String sha256 = MiscUtils.getSHA256FromString(fullUrl + requestBody);
        sha256 = sha256.replace("\\", "_");
        sha256 = sha256.replace("/", "_");
        String cacheFileName = getFileNameBy(fullUrl) + "_" + sha256 + ".cached";

        // load request if cache file exists
        Path cacheFilePath = Paths.get(cacheDirectory.toString(), cacheFileName);
        return cacheFilePath.toFile();
    }


}
