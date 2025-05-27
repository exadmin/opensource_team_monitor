package com.github.exadmin.ostm.github.badwords;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class BadWordsManager {
    private static final Logger log = LoggerFactory.getLogger(BadWordsManager.class);

    private static Map<String, String> badMap;

    public static void loadExpressionsFrom(String filePath) {
        badMap = new LinkedHashMap<>();

        try (FileInputStream fs = new FileInputStream(filePath)){
            Properties props = new Properties();
            props.load(fs);

            Set<Object> keys = props.keySet();
            for (Object key : keys) {
                badMap.put(key.toString(), props.get(key).toString());
            }
        } catch (IOException ex) {
            log.error("Error while loading bad words dictionary from {}", filePath, ex);
            throw new IllegalStateException(ex);
        }
    }

    public static Map<String, String> getBadMap() {
        return new HashMap<>(badMap);
    }
}
