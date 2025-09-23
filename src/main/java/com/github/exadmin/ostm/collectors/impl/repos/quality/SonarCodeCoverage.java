package com.github.exadmin.ostm.collectors.impl.repos.quality;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.exadmin.ostm.collectors.api.AbstractManyRepositoriesCollector;
import com.github.exadmin.ostm.github.api.GitHubRequest;
import com.github.exadmin.ostm.github.api.GitHubResponse;
import com.github.exadmin.ostm.github.api.HttpRequestBuilder;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.MiscUtils;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SonarCodeCoverage extends AbstractManyRepositoriesCollector {
    private static final int PER_PAGE = 50;
    private static final ObjectMapper mapper = new ObjectMapper(new JsonFactory());
    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private static final TypeReference<Map<String, Object>> type = new TypeReference<>() {};

    @Override
    public void collectDataIntoImpl(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        TheColumn column = theReportModel.findColumn(TheColumnId.COL_REPO_SONAR_CODE_COVERAGE_METRIC);

        // Step1: collect names of all repositories to ask statistics in the Sonar-Cloud for
        List<GitHubRepository> allRepos = gitHubFacade.getAllRepositories("Netcracker");

        // Step2: sort repositories by names - as it will better hits requests caching (at least on the local dev machine - where cache can be persisted)
        // not very useful for production - but anyway
        allRepos.sort(Comparator.comparing(GitHubRepository::getName));

        // Step2: do bulk requests with paging & delay between requests not to fall into throttling
        Map<String, String> componentToRowIdMap = new HashMap<>(); // this map contains Sonar Component Name to RepoId

        while (!allRepos.isEmpty()) {
            List<GitHubRepository> nextChunk = MiscUtils.getChunk(allRepos, PER_PAGE);
            if (nextChunk.isEmpty()) break;

            allRepos.removeAll(nextChunk);

            // convert names to long string and build request url
            StringBuilder sb = new StringBuilder();
            for (GitHubRepository repo : nextChunk) {
                String sonarComponentName = "Netcracker_" + repo.getName();
                if (!sb.isEmpty()) sb.append("%2C"); // comma delimiter
                sb.append(sonarComponentName);

                componentToRowIdMap.put(sonarComponentName, repo.getId());
            }
            String url = "https://sonarcloud.io/api/measures/search?metricKeys=coverage&projectKeys=" + sb;

            GitHubRequest request = HttpRequestBuilder.genericRESTCall().toURL(url).build();
            GitHubResponse response = request.execute();

            if (response.getHttpCode() == 200) {
                Map<String, Object> data = response.getDataMap().getFirst();
                List<Map<String, Object>> mList = (List<Map<String, Object>>) data.get("measures");
                for (Map<String, Object> mData : mList) {
                    String metricName = (String) mData.get("metric");
                    if ("coverage".equalsIgnoreCase(metricName)) {
                        String value = (String) mData.get("value");
                        int sortByValue = MiscUtils.getSortByValueForFloatString(value);
                        String component = (String) mData.get("component");

                        String rowId = componentToRowIdMap.remove(component);
                        column.setValue(rowId,
                                new TheCellValue(value + "%", sortByValue, SeverityLevel.INFO)
                                        .withHttpReference("https://sonarcloud.io/project/overview?id=" + component));
                    }
                }
            }

            // if we are requesting real data and there are more repos to request data for
            if (!response.isFromCache() && !allRepos.isEmpty()) {
                MiscUtils.sleep(2000);
            }
        }

        // at this step - componenToRowIdMap contains list of components with absent data
        for (Map.Entry<String, String> me : componentToRowIdMap.entrySet()) {
            column.setValue(me.getValue(),
                    new TheCellValue("No data", 0, SeverityLevel.ERROR)
                            .withHttpReference("https://sonarcloud.io/organizations/netcracker/projects"));
        }
    }
}
