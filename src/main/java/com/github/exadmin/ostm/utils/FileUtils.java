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

    public static String readFile(Path filePath) throws IOException {
        return readFile(filePath, false);
    }

    public static String readFile(Path filePath, boolean readMetadataForImages) throws IOException {
        // Check if this is an image file that should have metadata extracted
        if (readMetadataForImages) {
            String extension = getFileExtensionAsString(filePath);
            if (ImgUtils.isSupportedImageFormat(extension)) {
                // Extract and return metadata as searchable text
                String metadata = ImgUtils.extractMetadataAsText(filePath);

                // If metadata extraction succeeded, return it
                // Otherwise fall back to reading file as text (empty metadata means extraction failed)
                if (!metadata.isEmpty()) {
                    return metadata;
                }
            }
        }

        // For non-image files or if metadata extraction failed, read as text
        byte[] bytes = Files.readAllBytes(filePath);
        return new String(bytes);
    }

    /**
     * Handler to analyze is file is sutable to be returned during files collecting procedure
     * Note: fullFileName contain path delimiters which depend on operating system
     */
    public interface FileAcceptor {
        boolean testFileByName(String fullFileName, String shortFileName);
    }

    // todo: avoid String file names - switch to Path instances
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

                if (fileAcceptor.testFileByName(longFileName, shortFileName)) collectedFiles.add(longFileName);
            }
        }
    }

    public static void saveToFile(String content, String fileToWriteInto) throws IOException {
        Path path = Paths.get(fileToWriteInto);
        Files.write(path, content.getBytes());
    }

    public static String getFileExtensionAsString(Path path) {
        if (path == null) return null;

        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');

        return (dotIndex > 0 && dotIndex < fileName.length() - 1)
                ? fileName.substring(dotIndex + 1)
                : null;
    }
}
