package com.github.exadmin.ostm.github.signatures;

import com.github.exadmin.ostm.utils.FileUtils;
import com.github.exadmin.ostm.utils.PasswordBasedEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class AttentionSignaturesManager {
    private static final Logger log = LoggerFactory.getLogger(AttentionSignaturesManager.class);

    private static Map<String, Pattern> restrictedSigMap;
    private static Map<String, String> allowedSigMap;
    private static Map<String, List<String>> excludeExtsMap; // signature -> List of file extensions to ignore

    private static String dictionaryVersion = "undefined";

    public static void loadExpressionsFrom(String filePath, String password, String salt) {
        // decrypt file first
        try {
            String encryptedContent = FileUtils.readFile(filePath);
            String decryptedContent = PasswordBasedEncryption.decrypt(encryptedContent, password, salt);

            loadDecryptedContent(decryptedContent);
        } catch (Exception ex) {
            log.error("Error while reading file {}", filePath, ex);
            throw new IllegalStateException(ex);
        }
    }

    public static void loadDecryptedContent(String fileBody) {
        try (ByteArrayInputStream is = new ByteArrayInputStream(fileBody.getBytes(StandardCharsets.UTF_8))) {
            Properties properties = new Properties();
            properties.load(is);

            Map<String, Pattern> regExpTmpMap = new HashMap<>();
            Map<String, String> allowedSignaturesTmpMap = new HashMap<>();
            Map<String, List<String>> excludeExtTmpMap = new HashMap<>();

            for (Object key : properties.keySet()) {
                String sigId = key.toString();
                String expression = properties.getProperty(sigId);

                if ("version".equalsIgnoreCase(sigId)) {
                    dictionaryVersion = expression;
                    continue;
                }

                // if signature must exclude some files with provided list of extensions
                if (sigId.endsWith("(exclude-ext)")) {
                    String[] exts = expression.split(",");
                    List<String> extList = new ArrayList<>();
                    for (String ext : exts) {
                        ext = ext.trim();
                        if (!ext.isEmpty()) extList.add(ext);
                    }

                    sigId = sigId.substring(0, sigId.length() -13);
                    excludeExtTmpMap.put(sigId, extList);

                    continue;
                }

                if (sigId.endsWith("(allowed)")) {
                    sigId = sigId.substring(0, sigId.length() - 9);

                    log.info("Signature with id '{}' = '{}' is marked as allowed.", sigId, expression);
                    allowedSignaturesTmpMap.put(sigId, expression);
                } else {
                    compileAndKeep(sigId, expression, regExpTmpMap);
                }
            }

            restrictedSigMap = Collections.unmodifiableMap(regExpTmpMap);
            allowedSigMap = Collections.unmodifiableMap(allowedSignaturesTmpMap);
            excludeExtsMap = Collections.unmodifiableMap(excludeExtTmpMap);

        } catch (IOException ex) {
            log.error("Error while reading content", ex);
            throw new IllegalStateException(ex);
        }
    }

    public static Map<String, Pattern> getSignaturesMapCopy() {
        return new HashMap<>(restrictedSigMap);
    }

    public static Map<String, String> getAllowedSigMapCopy() {
        return new HashMap<>(allowedSigMap);
    }

    public static Map<String, List<String>> getExcludeExtsMap() {
        return excludeExtsMap;
    }

    private static final Set<Character> SPECIAL_CHARS =
            Set.of('!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '+', '=', '{', '}', '[', ']', '|', '\\', ':', ';', '"', '\'', '<', '>', ',', '.', '?', '/');

    private static void compileAndKeep(String key, String regExpStr, Map<String, Pattern> map) {
        final String originalKeyName = key; // for logging aims

        if (key.endsWith("(regexp)")) {
            key = key.substring(0, key.length() - 8);
        } else if (!key.contains("(") && !key.contains(")")) {

            // escape special characters if exists
            StringBuilder sb = new StringBuilder();
            for (char ch : regExpStr.toCharArray()) {
                for (char specialCh : SPECIAL_CHARS) {
                    if (ch == specialCh) {
                        sb.append("\\");
                        break;
                    }
                }

                sb.append(ch);
            }

            regExpStr = "\\b" + sb + "\\b";
        }

        try {
            log.info("Compiling key '{}' effective expressions = '{}'", originalKeyName, regExpStr);
            Pattern regExp = Pattern.compile(regExpStr, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
            map.put(key, regExp);
        } catch (PatternSyntaxException pse) {
            log.error("Error while compiling signature with ID = '{}', reg-exp = '{}'", originalKeyName, regExpStr);
        }
    }

    public static String getDictionaryVersion() {
        return dictionaryVersion;
    }
}
