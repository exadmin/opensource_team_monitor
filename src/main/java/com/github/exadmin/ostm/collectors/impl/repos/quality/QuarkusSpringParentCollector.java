package com.github.exadmin.ostm.collectors.impl.repos.quality;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.exadmin.ostm.collectors.api.AbstractOneRepositoryCollector;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.FileUtils;
import com.github.exadmin.ostm.utils.MiscUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public abstract class QuarkusSpringParentCollector extends AbstractOneRepositoryCollector {
    private static final TypeReference<Map<String, Object>> TYPE_REFERENCE = new TypeReference<>() {};
    private static final XmlMapper xmlMapper = new XmlMapper();

    protected abstract String getMavenGroupId();
    protected abstract List<String> getExtraProperties();

    @Override
    protected void processRepository(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path repositoryPath, GitHubRepository repository, TheColumn column) {
        // Algorithm
        // 1. select next repository
        // 2. list all pom.xml inside all folders
        // 3. load all <properties>...</> from each pom.xml
        // 4. grep all pom.xml for <groupId>org.springframework.boot</groupId>, <groupId>org.springframework</groupId>
        // 5. if version is found and scope != test - collect version into the unique set of versions

        List<String> allPomFiles = FileUtils.findAllFilesRecursively(repositoryPath.toString(), (fullFileName, shortFileName) -> shortFileName.equals("pom.xml"));
        Properties properties = collectAllProperties(allPomFiles);

        processOneMavenGroup(getMavenGroupId(), allPomFiles, properties, column, repository.getId(), getExtraProperties());
    }

    private void processOneMavenGroup(String groupId, List<String> allPomFiles, Properties properties, TheColumn theColumn, String rowId, List<String> extraProperties) {
        Set<String> versions = findVersionsFor(groupId, allPomFiles, properties, extraProperties);
        String text = String.join("<br>", versions);
        theColumn.addValue(rowId, new TheCellValue(text, versions.size(), SeverityLevel.INFO));
    }

    private Properties collectAllProperties(List<String> pomFiles) {
        Properties properties = new Properties(32);

        for (String fileName : pomFiles) {
            try (FileInputStream fs = new FileInputStream(fileName)) {
                Map<String, Object> fileMap = xmlMapper.readValue(fs, TYPE_REFERENCE);
                Object propsObj = MiscUtils.getValue(fileMap, "properties");
                if (propsObj == null) continue;
                if (propsObj instanceof String) {
                    getLog().info("Seems empty properties block in {}", fileName);
                    continue;
                }

                Map<String, Object> propsMap = (Map<String, Object>) propsObj;
                for (Map.Entry<String, Object> me : propsMap.entrySet()) {
                    String propertyKey = me.getKey();
                    String propertyValue = me.getValue().toString().trim();

                    // if threre is no such property name in properites set yet
                    if (!properties.containsKey(propertyKey)) {
                        properties.put(propertyKey, propertyValue);
                        continue;
                    }

                    // if duplicate propery found
                    String existedValue = properties.getProperty(propertyKey);
                    if (existedValue.equals(propertyValue)) continue; // it's ok - just duplicate

                    getLog().warn("Duplicate value for property {} in the file {}", propertyKey, fileName);
                }
            } catch (IOException ex) {
                getLog().error("Error while reading file {}", fileName, ex);
                throw new IllegalStateException(ex);
            }
        }

        return properties;
    }

    private Set<String> findVersionsFor(String groupId, List<String> allPomFiles, Properties properties, List<String> extraProperties) {
        Set<String> result = new HashSet<>();

        for (String fileName : allPomFiles) {
            try (FileInputStream fs = new FileInputStream(fileName)) {
                Map<String, Object> fileMap = xmlMapper.readValue(fs, TYPE_REFERENCE);

                // <parent>...</parent> block
                Object pDependencies = MiscUtils.getValue(fileMap, "parent");
                if (pDependencies != null) {
                    if (pDependencies instanceof Map) pDependencies = List.of(pDependencies);
                    String version = findVersionInDependencyList((List<Map<String, Object>>) pDependencies, properties, groupId);
                    if (version != null) result.add(version);
                }

                // <dependencyManagement> block
                Object mDependencies = MiscUtils.getValue(fileMap, "dependencyManagement/dependencies/dependency");
                if (mDependencies != null) {
                    if (mDependencies instanceof Map) mDependencies = List.of(mDependencies);
                    String version = findVersionInDependencyList((List<Map<String, Object>>) mDependencies, properties, groupId);
                    if (version != null) result.add(version);
                }

                // <dependencies> block
                Object dDependencies = MiscUtils.getValue(fileMap, "dependencies/dependency");
                if (dDependencies != null) {
                    if (dDependencies instanceof Map) dDependencies = List.of(dDependencies);
                    String version = findVersionInDependencyList((List<Map<String, Object>>) dDependencies, properties, groupId);
                    if (version != null) result.add(version);
                }



            } catch (IOException ex) {
                getLog().error("Error while reading file {}", fileName, ex);
                throw new IllegalStateException(ex);
            }
        }

        if (extraProperties != null) {
            for (String propertyKey : extraProperties) {
                String verFromProperties = properties.getProperty(propertyKey);
                if (verFromProperties != null) result.add(verFromProperties);
            }
        }

        return result;
    }

    private String findVersionInDependencyList(List<Map<String, Object>> dependencies, Properties properties, String groupId) {
        if (dependencies == null) return null;

        for (Map<String, Object> map : dependencies) {
            String gValue = MiscUtils.getValue(map, "groupId");
            // String artifactId = MiscUtils.getValue(map, "artifactId");
            String version = MiscUtils.getValue(map, "version");

            if (gValue.equals(groupId)) {
                // if (artifact.equals(artifactId)) {
                    if (StringUtils.isNotEmpty(version)) {
                        if (isProperty(version)) {
                            // get value from properties list
                            String value = getValueByProperty(properties, version);
                            if (value != null) return value;
                        } else {
                            return version.trim();
                        }
                    }
                // }
            }
        }

        return null;
    }

    private static boolean isProperty(String str) {
        if (str == null || str.length() < 3) return false;
        if (str.charAt(0) == '$' && str.charAt(1) == '{' && str.endsWith("}")) return true;

        return false;
    }

    private static String getValueByProperty(Properties properties, String propertyWithDollarSign) {
        String propertyKey = propertyWithDollarSign.substring(2, propertyWithDollarSign.length() - 1); // remove ${}
        return properties.getProperty(propertyKey);
    }
}
