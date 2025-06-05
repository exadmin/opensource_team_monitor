package com.github.exadmin.ostm.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    public static String readFile(String filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        return new String(bytes);
    }

    public interface FileAcceptor {
        boolean testFileByName(String shortFileName);
    }

    public static List<String> findAllFilesRecursively(String rootDirName, FileAcceptor fileAcceptor) {
        List<String> result = new ArrayList<>();

        _findAllFilesRecursively(result, new File(rootDirName), fileAcceptor);

        return result;
    }

    private static void _findAllFilesRecursively(List<String> collectedFiles, File dirToScan, FileAcceptor fileAcceptor) {
        log.trace("Continue collecting files in '{}'", dirToScan);
        File[] items = dirToScan.listFiles();
        if (items == null) return;

        for (File item : items) {
            log.trace("Processing item '{}'", item);
            if (item.isDirectory() && item.exists()) {
                _findAllFilesRecursively(collectedFiles, item, fileAcceptor);
            } else {
                String longFileName = item.toString();
                String shortFileName = item.getName();
                if (fileAcceptor.testFileByName(shortFileName)) collectedFiles.add(longFileName);
            }
        }
    }

    public static void saveToFile(String content, String fileToWriteInto) throws IOException {
        Path path = Paths.get(fileToWriteInto);
        Files.write(path, content.getBytes());
    }
}
