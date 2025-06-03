package com.github.exadmin.ostm.collectors.impl.repos.quality;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.exadmin.ostm.collectors.impl.repos.devops.AFilesContentChecker;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.MiscUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class PlatformVersionChecker extends AFilesContentChecker {
    private static final String SUFFIX = File.separator + "pom.xml";
    private static final XmlMapper xmlMapper = new XmlMapper();
    private static final TypeReference<Map<String, Object>> TYPE_REFERENCE = new TypeReference<>() {};

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_REPO_PLATFORM_SDK_VERSION);
    }

    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        List<String> foundPoms = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(Paths.get(repoDirectory.toString()))) {
            stream
                    .filter(Files::isRegularFile)
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .filter(s -> s.endsWith(SUFFIX))
                    .forEach(foundPoms::add);
        } catch (IOException ex) {
            getLog().error("Error while fetching files", ex);
            return new TheCellValue("Error:01", 0, SeverityLevel.ERROR);
        }

        for (String fileName : foundPoms) {
            try (FileInputStream fs = new FileInputStream(fileName)) {
                Map<String, Object> obj = xmlMapper.readValue(fs, TYPE_REFERENCE);
                String compilerRelease = getStringValue(obj, "maven.compiler.release");
                String compilerTarget = getStringValue(obj, "maven.compiler.target");
                String javaVersionProp = getStringValue(obj, "java.version");

                int javaVer = getJavaVersionAsInt(javaVersionProp, compilerTarget, compilerRelease);
                if (javaVer != 0) return new TheCellValue("Java " + javaVer, javaVer, SeverityLevel.INFO);
            } catch (IOException ex) {
                getLog().error("Error while reading file", ex);
                return new TheCellValue("Error:02", 1, SeverityLevel.ERROR);
            }
        }

        // there were poms but no info was found
        if (!foundPoms.isEmpty()) {
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
            return obj.toString();
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
}
