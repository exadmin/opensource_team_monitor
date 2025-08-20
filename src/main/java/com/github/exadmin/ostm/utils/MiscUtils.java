package com.github.exadmin.ostm.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

public class MiscUtils {
    private static final Logger log = LoggerFactory.getLogger(MiscUtils.class);

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

    public static String getStrValue(Map<String, Object> map, String keyName) {
        Object value = map.get(keyName);
        if (value == null) return null;
        return value.toString();
    }

    public static int getIntValue(Map<String, Object> map, String keyName) {
        Object value = map.get(keyName);
        if (value == null) return 0;
        return Integer.parseInt(value.toString());
    }

    public static boolean getBoolValue(Map<String, Object> map, String keyName) {
        Object value = map.get(keyName);
        if (value instanceof Boolean) {
            return (boolean) value;
        }

        if (value instanceof String) {
            String strValue = value.toString();
            if ("true".equalsIgnoreCase(strValue) || "false".equalsIgnoreCase(strValue)) {
                return Boolean.parseBoolean(strValue);
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getListValue(Map<String, Object> map, String keyName) {
        Object value = map.get(keyName);
        if (value == null) return Collections.emptyList();
        if (value instanceof List) {
            return (List<T>) value;
        }

        throw new IllegalArgumentException("List is expected but " + value.getClass() + " is found.");
    }

    public static String getSHA256AsBase64(String str) {
        try {
            try (InputStream is = new ByteArrayInputStream(str.getBytes())) {
                byte[] hash = getSha256FromInputStream(is);
                return Base64.getEncoder().encodeToString(hash);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String getSHA256AsHex(String str) {
        try {
            try (InputStream is = new ByteArrayInputStream(str.getBytes())) {
                byte[] hash = getSha256FromInputStream(is);
                return bytesToHex(hash);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static byte[] getSha256FromInputStream(InputStream is) throws IOException, NoSuchAlgorithmException {
        byte[] buffer= new byte[8192];
        int count;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (BufferedInputStream bis = new BufferedInputStream(is)) {
            while ((count = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
        }

        return digest.digest();
    }

    public static String getTokenFromArg(String filePathOrTokenItself) {
        try {
            // try to use a value as a file path
            File file = new File(filePathOrTokenItself);
            if (file.isFile() && file.exists()) return Files.readString(Paths.get(filePathOrTokenItself), StandardCharsets.UTF_8);

            // if cant - then return as a value itself
            return filePathOrTokenItself;
        } catch (IOException | NullPointerException ex) {
            log.error("Error while loading token from file {}", filePathOrTokenItself, ex);
        }

        return null;
    }

    /**
     * Return first non-null values from the provided set
     * @param values
     * @return
     * @param <T>
     */
    public static <T> T getFirstNonNull(T ... values) {
        for (T value : values) {
            if (value != null) return value;
        }

        return null;
    }

    /**
     * Removes all unprintables characters from the source string.*
     * @param originalString
     * @return
     */
    public static String getLettersOnly(String originalString) {
        StringBuilder sb = new StringBuilder();
        for (char ch : originalString.toCharArray()) {
            if (Character.isLetter(ch)) sb.append(ch);
        }

        return sb.toString();
    }

    public static Integer getCharSum(String inStr) {
        int sum = 0;
        for (char ch : inStr.toCharArray()) {
            sum = sum + ch;
        }

        return sum;
    }
}
