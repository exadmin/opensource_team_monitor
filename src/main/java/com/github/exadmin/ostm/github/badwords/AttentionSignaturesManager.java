package com.github.exadmin.ostm.github.badwords;

import com.github.exadmin.ostm.utils.FileUtils;
import com.github.exadmin.ostm.utils.PasswordBasedEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class AttentionSignaturesManager {
    private static final Logger log = LoggerFactory.getLogger(AttentionSignaturesManager.class);

    private static Map<String, Pattern> badMap;

    public static void loadExpressionsFrom(String filePath, String password, String salt) {
        // decrypt file first
        try {
            String encryptedContent = FileUtils.readFile(filePath);
            String decryptedContent = PasswordBasedEncryption.decrypt(encryptedContent, password, salt);

            badMap = loadDecryptedContent(decryptedContent);
        } catch (Exception ex) {
            log.error("Error while reading file {}", filePath, ex);
            throw new IllegalStateException(ex);
        }
    }

    public static Map<String, Pattern> loadDecryptedContent(String fileBody) {
        Map<String, Pattern> result = new LinkedHashMap<>();

        try (ByteArrayInputStream is = new ByteArrayInputStream(fileBody.getBytes(StandardCharsets.UTF_8))) {
            Properties props = new Properties();
            props.load(is);

            Set<Object> keys = props.keySet();
            for (Object key : keys) {
                try {
                    String regExp = props.get(key).toString();
                    Pattern pattern = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
                    result.put(key.toString(), pattern);
                    log.debug("RegExp {} is compiled successfully", key);
                } catch (Exception ex) {
                    log.error("Error while compiling  reg-exp with id = {}", key);
                }
            }
        } catch (IOException ex) {
            log.error("Error while reading content", ex);
            throw new IllegalStateException(ex);
        }

        return result;
    }

    public static Map<String, Pattern> getBadMap() {
        return new HashMap<>(badMap);
    }
}
