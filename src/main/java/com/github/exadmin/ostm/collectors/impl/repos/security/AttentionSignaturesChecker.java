package com.github.exadmin.ostm.collectors.impl.repos.security;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.exadmin.ostm.collectors.impl.repos.devops.AFilesContentChecker;
import com.github.exadmin.ostm.git.GitFacade;
import com.github.exadmin.ostm.git.GitRepository;
import com.github.exadmin.ostm.github.signatures.AttentionSignaturesManager;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.FileUtils;
import com.github.exadmin.ostm.utils.MiscUtils;
import com.github.exadmin.sourcesscanner.exclude.ExcludeFileModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// todo: optimize checking by using: "git rev-parse --short HEAD"
public class AttentionSignaturesChecker extends AFilesContentChecker {

    private static final Logger log = LoggerFactory.getLogger(AttentionSignaturesChecker.class);
    private TheColumn theColumn;

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        this.theColumn = theReportModel.findColumn(TheColumnId.COL_REPO_SEC_SIGNATURES_CHECKER);
        return this.theColumn;
    }

    @Override
    protected TheCellValue checkOneRepository(GitRepository repo, GitFacade gitFacade, Path repoDirectory) {
        if ("disable".equalsIgnoreCase(System.getenv("BWC"))) return new TheCellValue("Disabled", 0, SeverityLevel.WARN);

        // load exclusions configuration if exists
        Path exPath = Paths.get(repoDirectory.toString(), ".qubership", "grand-report.json");
        ExcludeFileModel efModel = exPath.toFile().exists() ? loadExistedModel(exPath) : null;

        Map<String, Pattern> sigMapCopy = AttentionSignaturesManager.getSignaturesMapCopy();
        final String repoDir = repoDirectory.toString();

        // set dictionary version into column
        theColumn.setTitle("Attention signatures (v." + AttentionSignaturesManager.getDictionaryVersion() + ")");

        List<Path> filesToAnalyze = new ArrayList<>();

        try {
            Files.walkFileTree(repoDirectory, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    // todo: move this hard code to some configurable place, priority = normal
                    if (dir.getFileName().toString().equals(".git")) return FileVisitResult.SKIP_SUBTREE;

                    String relFileName = getRelativeFileName(repoDirectory, dir);
                    String fileHash = MiscUtils.getSHA256AsHex(relFileName);

                    if (efModel != null && efModel.isPathFullyIgnored(fileHash)) return FileVisitResult.SKIP_SUBTREE;
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String relFileName = getRelativeFileName(repoDirectory, file);
                    String fileHash = MiscUtils.getSHA256AsHex(relFileName);
                    if (efModel == null || !efModel.isPathFullyIgnored(fileHash)) filesToAnalyze.add(file);

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            log.error("Error while walking in the directory {}", repoDir, ex);
        }

        Map<String, String> foundSigs = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        final Map<String, String> allowedSigMap = AttentionSignaturesManager.getAllowedSigMapCopy();
        final AtomicInteger numberOfWarnings = new AtomicInteger(0);

        for (Path nextFileName : filesToAnalyze) {
            if (sigMapCopy.isEmpty()) break; // no signatures to search for
            if (!foundSigs.isEmpty()) break;; // we just highlight that at least something was found

            final String fileContent;
            try {
                fileContent = FileUtils.readFile(nextFileName, true);
            } catch (Exception ex) {
                getLog().error("Error while reading file content of {}", nextFileName, ex);
                return new TheCellValue("Internal error", 1, SeverityLevel.ERROR);
            }

            CompletableFuture<?>[] futures = sigMapCopy.entrySet().stream()
                    .map(me -> CompletableFuture.supplyAsync( () ->
                            {
                                final String sigId = me.getKey();
                                if (isToIgnoreFile(sigId, nextFileName)) return null;

                                final String relFileName = getRelativeFileName(repoDirectory, nextFileName);
                                final String fileHash = MiscUtils.getSHA256AsHex(relFileName);

                                // check if this file is fully marked to be ignored
                                if (efModel != null && efModel.contains(ExcludeFileModel.SKIP_FULL_FILE_HASH, fileHash))
                                    return null;

                                // otherwise - try scan it for signatures
                                Matcher matcher = me.getValue().matcher(fileContent);
                                if (matcher.find()) {
                                    String foundText = matcher.group();
                                    if (allowedSigMap.containsValue(foundText)) return null;

                                    String textHash = MiscUtils.getSHA256AsHex(foundText);

                                    // if current signature is in the exclusion list
                                    if (efModel != null && efModel.contains(textHash, fileHash)) {
                                        getLog().debug("Pattern-id {} was skipped for the file {} as it was found in the exclusion lists", me.getKey(), nextFileName);
                                        return null;
                                    } else {
                                        String hash = calculateSignatureHash(repoDir, nextFileName.toString(), matcher);
                                        foundSigs.put(me.getKey(), hash);
                                        numberOfWarnings.incrementAndGet();
                                        getLog().debug("Pattern-id {} was detected in a file {}", me.getKey(), nextFileName);
                                    }
                                }

                                return null;
                            }, executor)
                    )
                    .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();

            sigMapCopy.keySet().removeAll(foundSigs.keySet()); // reduce number of signatures to work with in scope of this repository
        }

        if (numberOfWarnings.get() > 0) {
            // String sb = "Warnings (" + numberOfWarnings.get() + ")";
            String sb = "Warnings found";
            return new TheCellValue(sb, numberOfWarnings.get(), SeverityLevel.WARN);
        }

        return new TheCellValue("Ok", 0, SeverityLevel.OK);
    }

    private static boolean isToIgnoreFile(String signatureId, Path currentFile) {
        Map<String, List<String>> excludeExtMap = AttentionSignaturesManager.getExcludeExtsMap();
        if (excludeExtMap == null) return false;

        List<String> list = excludeExtMap.get(signatureId);
        if (list == null) return false;

        String fileExt = FileUtils.getFileExtensionAsString(currentFile);
        if (fileExt == null) return false;

        return list.contains(fileExt);
    }

    /**
     * Makes double-check - if found value is not false-positive.
     * For instance, there is "IP-Address" reg-exp, but some addresses are valid to be published in the repository.
     * This listener allows not make additional check without complication of initial regexp.
     * @param filePath
     * @param fileContent
     * @param patternId
     * @param regExp
     * @param matcher
     * @return
     */
    protected boolean approveFoundPattern(String filePath, String fileContent, String patternId, Pattern regExp, Matcher matcher) {
        // analyze IP addresses for false-positives
        if ("OTH-IP-ADDR".equals(patternId)) {
            String ipAddressValue = matcher.group();
            getLog().debug("Checking IP Address value = {}", ipAddressValue);
            if ("0.0.0.0".equals(ipAddressValue) || "127.0.0.1".equals(ipAddressValue)) return false;
        }

        if ("INT-004".equals(patternId)) {
            String value = matcher.group().toLowerCase();
            if ("pages.netcracker.com".equalsIgnoreCase(value)) return false;
        }

        if ("INT-006".equals(patternId)) {
            String value = matcher.group().toLowerCase();
            if ("opensourcegroup@netcracker.com".equals(value)) return false;
        }

        return true;
    }

    private static String calculateSignatureHash(String repoDir, String fullFileName, Matcher matcher) {
        repoDir = repoDir.trim();
        fullFileName = fullFileName.trim();

        String relFileName = fullFileName.substring(repoDir.length());
        relFileName = relFileName.replace("\\", "/"); // switch to linux style

        int startOffset = matcher.start();
        int endOffset   = matcher.end();

        String testString = relFileName + ":" + startOffset + ":" + endOffset;
        return MiscUtils.getSHA256AsHex(testString).substring(0, 16); // return only first 16 chars

    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static ExcludeFileModel loadExistedModel(Path filePath) {
        try {
            OBJECT_MAPPER.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature());
            return OBJECT_MAPPER.readValue(filePath.toFile(), ExcludeFileModel.class);
        } catch (Exception ex) {
            log.error("Can't load existed exclusion configuration {}", filePath, ex);
            return null;
        }
    }

    public static String getRelativeFileName(Path rootDir, Path fileName) {
        // Normalize both paths to handle '/./' and '/../' components
        Path normRoot = rootDir.normalize();
        Path normFile = fileName.normalize();

        // Make sure the file path is within the root directory
        if (normFile.startsWith(normRoot)) {
            // Relativize the path (returns the relative path from rootDir to fileName)
            Path relativePath = normRoot.relativize(normFile);
            String result = relativePath.toString();
            return result.replace("\\", "/"); // switch to unix style in windows running case
        }

        throw new IllegalStateException("Unexpected file path " + normFile);
    }
}
