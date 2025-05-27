package com.github.exadmin.ostm.github.badwords;

import com.github.exadmin.ostm.utils.FileUtils;
import com.github.exadmin.ostm.utils.PasswordBasedEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BadWordsManager {
    private static final Logger log = LoggerFactory.getLogger(BadWordsManager.class);

    private static Map<String, String> badMap;

    public static void loadExpressionsFrom(String filePath, String password, String salt) {
        badMap = new LinkedHashMap<>();

        // decrypt file first
        try {
            String encryptedContent = FileUtils.readFile(filePath);
            String decryptedContent = PasswordBasedEncryption.decrypt(encryptedContent, password, salt);


            try (ByteArrayInputStream is = new ByteArrayInputStream(decryptedContent.getBytes(StandardCharsets.UTF_8))) {
                Properties props = new Properties();
                props.load(is);

                Set<Object> keys = props.keySet();
                for (Object key : keys) {
                    badMap.put(key.toString(), props.get(key).toString());
                }
            } catch (IOException ex) {
                log.error("Error while loading bad words dictionary from {}", filePath, ex);
                throw new IllegalStateException(ex);
            }
        } catch (Exception ex) {
            log.error("Error while reading file {}", filePath, ex);
            throw new IllegalStateException(ex);
        }
    }

    public static Map<String, String> getBadMap() {
        return new HashMap<>(badMap);
    }
}
