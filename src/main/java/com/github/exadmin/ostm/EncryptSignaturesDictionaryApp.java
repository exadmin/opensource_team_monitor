package com.github.exadmin.ostm;

import com.github.exadmin.ostm.github.signatures.AttentionSignaturesManager;
import com.github.exadmin.ostm.utils.FileUtils;
import com.github.exadmin.ostm.utils.MiscUtils;
import com.github.exadmin.ostm.utils.PasswordBasedEncryption;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptSignaturesDictionaryApp {
    private static final int ARG1_SOURCE_PROPERTIES_FILE = 0;
    private static final int ARG2_OUTPUT_ENCRYPTED_FILE  = 1;
    private static final int ARG3_PASSWORD_OR_FILE_WITH_PASSWORD = 2;
    private static final int ARG4_SALT_OR_FILE_WITH_SALT = 3;

    private static final Logger log = LoggerFactory.getLogger(EncryptSignaturesDictionaryApp.class);

    public static void main(String[] args) {
        String sourceFile = args[ARG1_SOURCE_PROPERTIES_FILE];
        String outputFile = args[ARG2_OUTPUT_ENCRYPTED_FILE];
        String password   = MiscUtils.getTokenFromArg(args[ARG3_PASSWORD_OR_FILE_WITH_PASSWORD]);
        String salt       = MiscUtils.getTokenFromArg(args[ARG4_SALT_OR_FILE_WITH_SALT]);

        if (StringUtils.isEmpty(sourceFile)) {
            log.error("Source file path to read for encryption is not set. Terminating...");
            System.exit(-1);
        }

        if (StringUtils.isEmpty(outputFile)) {
            log.error("Output file path to create is not set. Terminating...");
            System.exit(-2);
        }

        if (StringUtils.isEmpty(password)) {
            log.error("Password is not set. Terminating...");
            System.exit(-3);
        }

        if (StringUtils.isEmpty(salt)) {
            log.error("Salt for password is not set. Terminating...");
            System.exit(-4);
        }



        String srcContent = null;
        try {
            srcContent = FileUtils.readFile(sourceFile);

            log.info("Testing source file for reg-exp compilation '{}'", sourceFile);
            AttentionSignaturesManager.loadDecryptedContent(srcContent);
        } catch (Exception ex) {
            log.error("Error while reading source file {}", sourceFile, ex);
            System.exit(-5);
        }

        try {
            String encContent = PasswordBasedEncryption.encrypt(srcContent, password, salt);
            if (encContent != null) FileUtils.saveToFile(encContent, outputFile);

            if (encContent == null) {
                log.error("Encryption result is null. Terminating...");
                System.exit(-7);
            }
        } catch (Exception ex) {
            log.error("Error while writing output file {}", outputFile, ex);
            System.exit(-6);
        }

        // testing result
        try {
            String encryptedContent = FileUtils.readFile(outputFile);
            String actContent = PasswordBasedEncryption.decrypt(encryptedContent, password, salt);
            if (srcContent.equals(actContent)) {
                log.info("Ecnypted string was successfully decrypted to the same content");
            } else {
                throw new Exception("Decrypted string differs to original one!");
            }
        } catch (Exception ex) {
            log.error("Error while testing created results. Do not use output file {}", outputFile, ex);
            System.exit(-8);
        }

        log.info("Encryption is done. Find results at {}", outputFile);
    }
}
