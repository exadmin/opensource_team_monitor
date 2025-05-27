package com.github.exadmin.ostm.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

public class PasswordBasedEncryption {
    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;
    private static final byte[] IV = {0, 2, 3, 4, 5, 4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 0};

    private static final Logger log = LoggerFactory.getLogger(PasswordBasedEncryption.class);

    public static String encrypt(String strToEncrypt, String password, String salt) {
        try {
            // Create secret key from password
            SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), ITERATION_COUNT, KEY_LENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), ALGORITHM);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            // Perform encryption
            byte[] encryptedBytes = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception ex) {
            log.error("Error while encyption", ex);
            return null;
        }
    }

    public static String decrypt(String strToDecrypt, String password, String salt) {
        try {
            // Create secret key from password
            SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), ITERATION_COUNT, KEY_LENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), ALGORITHM);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            // Perform decryption
            byte[] decodedBytes = Base64.getDecoder().decode(strToDecrypt);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            log.error("Error while decryption", ex);
        }
        return null;
    }

    /*public static void main(String[] args) {
        String originalString = "This is a secret message!";
        String password = "MyStrongPassword123";
        String salt = "hello!";

        System.out.println("Original String: " + originalString);

        // Encryption
        String encryptedString = encrypt(originalString, password, salt);
        System.out.println("Encrypted String: " + encryptedString);

        // Decryption
        String decryptedString = decrypt(encryptedString, password, salt);
        System.out.println("Decrypted String: " + decryptedString);
    }*/
}