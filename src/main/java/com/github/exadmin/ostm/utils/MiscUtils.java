package com.github.exadmin.ostm.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MiscUtils {
    private static final Logger log = LoggerFactory.getLogger(MiscUtils.class);
    private static MessageDigest digest = null;

    public static String getSHA256(String inStr) {
        if (digest == null) {
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException ex) {
                log.error("Error while instantiating SHA-256 digest", ex);
                throw new IllegalStateException(ex);
            }
        }

        byte[] hashBytes = digest.digest(inStr.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hashBytes);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String dateToStr(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
    }

    public static LocalDate strToDate(String strDateISO6801) {
        if (strDateISO6801.length() == 10) strDateISO6801 = strDateISO6801 + "T00:00:00.000+00:00"; // to fix strange case for graphQL response from GitHub

        return OffsetDateTime.parse(strDateISO6801).toLocalDate();
    }

    /**
     * Goes over data-map by path provided as string with "/" or "\" delimiters
     * @param fromMap source map to start navigation deep into by keys
     * @param path String path which represents keys
     * @return instance of required type or null of somewhere null will be met
     */
    public static <T> T getValue(Map<String, Object> fromMap, String path) {
        // normalize paths delimiters to "/"
        path = path.replace("\\", "/");

        String[] keys = path.split("/");
        keys = Arrays.stream(keys).filter(Predicate.not(String::isEmpty)).toList().toArray(new String[0]);
        return getObject(fromMap, keys);
    }

    /**
     * Goes over data-map by path provided with keys and returns found result
     * @param keys list of key names.
     * @return single value of required type
     */
    @SuppressWarnings("unchecked")
    private static <T> T getObject(Map<String, Object> dataMap, String ... keys) {
        final Map<String, Object> sourceMapForDebugging = dataMap;

        Object currentObj = null;

        for (String key : keys) {
            if (dataMap == null) return null;
            currentObj = dataMap.get(key);

            if (currentObj == null) return null;
            if (currentObj instanceof Map) {
                dataMap = (Map<String, Object>) currentObj;
            } else {
                dataMap = null;
            }
        }

        try {
            if (currentObj == null) return null;

            return (T) currentObj;
        } catch (ClassCastException ex) {
            log.error("Class-cast exception while returning value from data-map. Current value is {}, type is {}", currentObj, currentObj.getClass(), ex);
            throw new IllegalStateException(ex);
        }
    }

    public static <K, V> Map<K, V> sortMapByValues(Map<K, V> sourceMap, Comparator<Map.Entry<K, V>> comparator) {
        List<Map.Entry<K, V>> list = new ArrayList<>(sourceMap.entrySet());

        list.sort(comparator);

        Map<K, V> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    /**
     * Returns X first existed elements from source list.
     * @param source Source list to get elements from
     * @param itemsPerChunk max number of elements to be returned
     * @return
     */
    public static <T> List<T> getChunk(List<T> source, int itemsPerChunk) {
        List<T> result = new ArrayList<>();
        for (int i=0; i<itemsPerChunk; i++) {
            if (i < source.size()) {
                result.add(source.get(i));
            } else  break;
        }

        return result;
    }

    public static void sleep(int milliSecs) {
        try {
            Thread.sleep(milliSecs);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }
}
