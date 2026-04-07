package com.github.exadmin.ostm;

import com.github.exadmin.ostm.github.signatures.AttentionSignaturesManager;
import com.github.exadmin.ostm.utils.FileUtils;
import com.github.exadmin.ostm.utils.MiscUtils;
import com.github.exadmin.ostm.utils.PasswordBasedEncryption;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class EncryptSignaturesDictionaryApp {
    private static final int ARG1_SOURCE_PROPERTIES_FILE = 0;
    private static final int ARG2_OUTPUT_ENCRYPTED_FILE  = 1;
    private static final int ARG3_PASSWORD_OR_FILE_WITH_PASSWORD = 2;

    private static final Logger log = LoggerFactory.getLogger(EncryptSignaturesDictionaryApp.class);

    public static void main(String[] args) {
        if (!validateArgs(args)) {
            System.exit(-1);
        }

        String sourceFile = args[ARG1_SOURCE_PROPERTIES_FILE];
        String outputFile = args[ARG2_OUTPUT_ENCRYPTED_FILE];
        String password   = MiscUtils.getTokenFromArg(args[ARG3_PASSWORD_OR_FILE_WITH_PASSWORD]);

        try {
            processFiles(sourceFile, outputFile, password);
        } catch (Exception ex) {
            log.error("An error occurred during processing. Terminating...", ex);
            System.exit(-1);
        }
    }

    private static boolean validateArgs(String[] args) {
        if (StringUtils.isEmpty(args[ARG1_SOURCE_PROPERTIES_FILE])) {
            log.error("Source file path to read for encryption is not set. Terminating...");
            return false;
        }

        if (StringUtils.isEmpty(args[ARG2_OUTPUT_ENCRYPTED_FILE])) {
            log.error("Output file path to create is not set. Terminating...");
            return false;
        }

        if (StringUtils.isEmpty(MiscUtils.getTokenFromArg(args[ARG3_PASSWORD_OR_FILE_WITH_PASSWORD]))) {
            log.error("Password is not set. Terminating...");
            return false;
        }

        return true;
    }

    private static void processFiles(String sourceFile, String outputFile, String password) throws Exception {
        String srcContent = FileUtils.readFile(Paths.get(sourceFile));

        log.info("Testing source file for reg-exp compilation '{}'", sourceFile);
        AttentionSignaturesManager.loadDecryptedContent(srcContent);

        String encContent = PasswordBasedEncryption.encrypt(srcContent, password);
        if (encContent != null) {
            FileUtils.saveToFile(encContent, outputFile);
        } else {
            throw new Exception("Encryption result is null.");
        }

        // Testing result
        String encryptedContent = FileUtils.readFile(Paths.get(outputFile));
        String actContent = PasswordBasedEncryption.decrypt(encryptedContent, password);
        if (!srcContent.equals(actContent)) {
            throw new Exception("Decrypted string differs to original one!");
        }

        log.info("Encryption is done. Find results at {}", outputFile);
    }
}
