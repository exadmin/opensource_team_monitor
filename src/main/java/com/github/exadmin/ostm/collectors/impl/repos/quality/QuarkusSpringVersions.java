package com.github.exadmin.ostm.collectors.impl.repos.quality;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.exadmin.ostm.collectors.api.AbstractCollector;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.FileUtils;
import com.github.exadmin.ostm.utils.MiscUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class QuarkusSpringVersions extends AbstractCollector {
    private static final TypeReference<Map<String, Object>> TYPE_REFERENCE = new TypeReference<>() {};
    private static final XmlMapper xmlMapper = new XmlMapper();

    @Override
    public void collectDataInto(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        TheColumn colSpring = theReportModel.findColumn(TheColumnId.COL_REPO_QUALITY_SPRING_FRAMEWORK_VERSION);
        TheColumn colQuarkus= theReportModel.findColumn(TheColumnId.COL_REPO_QUALITY_QUARKUS_FRAMEWORK_VERSION);

        List<GitHubRepository> allRepositories = gitHubFacade.getAllRepositories("Netcracker");
        for (GitHubRepository repo : allRepositories) {
            String rowId = repo.getId();

            // collect all pom.xml files inside repository folder
            String repoFolder = Paths.get(parentPathForClonedRepositories.toString(), repo.getName()).toString();
            List<String> files = FileUtils.findAllFilesRecursively(repoFolder, shortFileName -> shortFileName.equals("pom.xml"));

            // try to get spring or quarkus version from them
            Result result = new Result("", "", 0, 0);

            for (String fileName : files) {
                try (FileInputStream fs = new FileInputStream(fileName)) {
                    Map<String, Object> obj = xmlMapper.readValue(fs, TYPE_REFERENCE);

                    // load dependencies declarations
                    Map<String, Object> properties    = MiscUtils.getValue(obj, "properties");
                    Object mDependencies = MiscUtils.getValue(obj, "dependencyManagement/dependencies/dependency");
                    Object dDependencies = MiscUtils.getValue(obj, "dependencies/dependency");

                    if (mDependencies instanceof Map) mDependencies = List.of(mDependencies);
                    if (dDependencies instanceof Map) dDependencies = List.of(dDependencies);

                    parseFrameworkVersions((List) mDependencies, (List) dDependencies, properties, result);
                } catch (Exception ex) {
                    getLog().error("Error while reading file {}", fileName, ex);
                }
            }

            colSpring.addValue(rowId, new TheCellValue(result.springVersion, result.springVersionToSortBy, SeverityLevel.INFO));
            colQuarkus.addValue(rowId, new TheCellValue(result.quarkusVersion, result.quarkusVersionToSortBy, SeverityLevel.INFO));
        }
    }

    private void parseFrameworkVersions(List<Map<String, Object>> mDependencies, List<Map<String, Object>> dDependencies, Map<String, Object> properties, Result result) {
        // define spring-framework version
        String sVersion = findVersionInDependencyList(mDependencies, properties, "org.springframework.cloud", "spring-cloud-dependencies");
        if (sVersion != null) {
            result.springVersion = result.springVersion + sVersion;
            result.springVersionToSortBy = 1;
        } else {
            sVersion = findVersionInDependencyList(dDependencies, properties, "org.springframework.cloud", "spring-cloud-dependencies");
            if (sVersion != null) {
                result.springVersion = result.springVersion + sVersion;
                result.springVersionToSortBy = 1;
            }
        }

        // define quarkus-framework version
        String qVersion = MiscUtils.getStrValue(properties, "quarkus.version");
        if (qVersion != null) {
            result.quarkusVersion = result.quarkusVersion + qVersion.trim();
            result.quarkusVersionToSortBy = 2;
        }
    }

    private String findVersionInDependencyList(List<Map<String, Object>> dependencies, Map<String, Object> properties, String group, String artifact) {
        if (dependencies == null) return null;

        for (Map<String, Object> map : dependencies) {
            String groupId = MiscUtils.getValue(map, "groupId");
            String artifactId = MiscUtils.getValue(map, "artifactId");
            String version = MiscUtils.getValue(map, "version");

            if (group.equals(groupId)) {
                if (artifact.equals(artifactId)) {
                    if (StringUtils.isNotEmpty(version)) {
                        if (isProperty(version) && properties != null) {
                            // get value from properties list
                            String value = getValueByProperty(properties, version);
                            if (value != null) return value;
                        } else {
                            return version.trim();
                        }
                    }
                }
            }
        }

        return null;
    }

    private static boolean isProperty(String str) {
        if (str == null || str.length() < 3) return false;
        if (str.charAt(0) == '$' && str.charAt(1) == '{' && str.endsWith("}")) return true;

        return false;
    }

    private static String getValueByProperty(Map<String, Object> properties, String propertyWithDollarSign) {
        // remove ${}
        String justProperty = propertyWithDollarSign.substring(2, propertyWithDollarSign.length() - 1);

        return MiscUtils.getValue(properties, justProperty);
    }

    private static class Result {
        String springVersion;
        String quarkusVersion;
        Integer springVersionToSortBy;
        Integer quarkusVersionToSortBy;

        public Result(String springVersion, String quarkusVersion, Integer springVersionToSortBy, Integer quarkusVersionToSortBy) {
            this.springVersion = springVersion;
            this.quarkusVersion = quarkusVersion;
            this.springVersionToSortBy = springVersionToSortBy;
            this.quarkusVersionToSortBy = quarkusVersionToSortBy;
        }
    }
}
