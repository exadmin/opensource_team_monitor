package com.github.exadmin.ostm.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
}
