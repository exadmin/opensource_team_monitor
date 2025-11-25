package com.github.exadmin.ostm.collectors.impl.repos.quality;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.exadmin.ostm.collectors.impl.repos.devops.AFilesContentChecker;
import com.github.exadmin.ostm.git.GitFacade;
import com.github.exadmin.ostm.git.GitRepository;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.FileUtils;
import com.github.exadmin.ostm.utils.MiscUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class LanguagePlatformVersionChecker extends AFilesContentChecker {
    private static final String JAVA_SUFFIX = File.separator + "pom.xml";
    private static final String GO_SUFFIX = File.separator + "go.mod";
    private static final XmlMapper xmlMapper = new XmlMapper();
    private static final TypeReference<Map<String, Object>> TYPE_REFERENCE = new TypeReference<>() {};
    private static final Pattern GO_LANG = Pattern.compile("^\\s*go\\s+(\\d+\\.\\d+(\\.\\d+)?)$", Pattern.MULTILINE);

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_REPO_PLATFORM_SDK_VERSION);
    }

    @Override
    protected TheCellValue checkOneRepository(GitRepository repo, GitFacade gitFacade, Path repoDirectory) {
        List<String> filesOfInterest = new ArrayList<>();

        if (!repoDirectory.toFile().exists()) {
            return new TheCellValue("Was not downloaded", 0, SeverityLevel.ERROR);
        }

        try (Stream<Path> stream = Files.walk(repoDirectory)) {
            stream
                    .filter(Files::isRegularFile)
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .filter(s -> s.endsWith(JAVA_SUFFIX) || s.endsWith(GO_SUFFIX))
                    .forEach(filesOfInterest::add);
        } catch (IOException ex) {
            getLog().error("Error while fetching files", ex);
            return new TheCellValue("Error:01", 0, SeverityLevel.ERROR);
        }

        for (String fileName : filesOfInterest) {
            if (fileName.endsWith(JAVA_SUFFIX)) {
                try (FileInputStream fs = new FileInputStream(fileName)) {
                    Map<String, Object> obj = xmlMapper.readValue(fs, TYPE_REFERENCE);
                    String compilerRelease = getStringValue(obj, "maven.compiler.release");
                    String compilerTarget = getStringValue(obj, "maven.compiler.target");
                    String javaVersionProp = getStringValue(obj, "java.version");

                    int javaVer = getJavaVersionAsInt(javaVersionProp, compilerTarget, compilerRelease);
                    if (javaVer != 0) return new TheCellValue("Java " + javaVer, javaVer, SeverityLevel.INFO);
                } catch (IOException ex) {
                    getLog().error("Error while reading file {}", fileName, ex);
                    return new TheCellValue("Error:02", 1, SeverityLevel.ERROR);
                }
            }

            if (fileName.endsWith(GO_SUFFIX)) {
                try {
                    String content = FileUtils.readFile(Paths.get(fileName));
                    Matcher matcher = GO_LANG.matcher(content);
                    if (matcher.find()) {
                        String goVer = matcher.group(1);
                        return new TheCellValue("Go " + goVer, getGoVersionAsInt(goVer), SeverityLevel.INFO);
                    }
                } catch (Exception ex) {
                    getLog().error("Error while reading file {}", fileName, ex);
                    return new TheCellValue("Error:03", 2, SeverityLevel.ERROR);
                }
            }
        }

        // there were poms but no info was found
        if (!filesOfInterest.isEmpty()) {
            return new TheCellValue("Unclear format", 2, SeverityLevel.ERROR);
        }

        // seems it's not java-project
        return new TheCellValue("Not a Java project", 3, SeverityLevel.WARN);
    }

    private static String getStringValue(Map<String, Object> xmlMap, String propertyNameInPropertiesSection) {
        Object obj = xmlMap.get("properties");
        if (obj instanceof Map) {
            Map propertiesXmlMap = (Map) obj;
            Object propertyValue = propertiesXmlMap.get(propertyNameInPropertiesSection);
            if (propertyValue != null) return propertyValue.toString();
        }

        if (obj instanceof String) {
            String str = obj.toString();

            if (str.isBlank()) return null;
            return str;
        }

        return null;
    }

    private Integer getJavaVersionAsInt(String javaVersion, String compilerTarget, String compilerRelease) {
        // define java-version as string first
        if ("${java.version}".equalsIgnoreCase(compilerRelease)) compilerRelease = javaVersion;
        if ("${java.version}".equalsIgnoreCase(compilerTarget)) compilerTarget = javaVersion;

        javaVersion = MiscUtils.getFirstNonNull(compilerRelease, compilerTarget, javaVersion);
        if (javaVersion == null) return 0;

        // convert string to int
        if (javaVersion.startsWith("1.")) javaVersion = javaVersion.substring(2);
        try {
            return Integer.parseInt(javaVersion);
        } catch (NumberFormatException ex) {
            getLog().error("Error while parsing java-version from {}", javaVersion, ex);
            return 0;
        }
    }

    private Integer getGoVersionAsInt(String goVer) {
        try {
            goVer = goVer.replace(".", "");
            return Integer.parseInt(goVer);
        } catch (NumberFormatException ex) {
            getLog().error("Error while parsing go-version as a number {}", goVer, ex);
            return 0;
        }
    }
}
