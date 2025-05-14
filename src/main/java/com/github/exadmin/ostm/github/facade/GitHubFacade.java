package com.github.exadmin.ostm.github.facade;

import com.github.exadmin.ostm.github.api.GitHubRequest;
import com.github.exadmin.ostm.github.api.GitHubRequestBuilder;
import com.github.exadmin.ostm.github.api.GitHubResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GitHubFacade {
    private static final int CACHE_TTL_SECONDS = 5/*hours*/ * 60/*minutes*/ * 60/*seconds*/;

    public GitHubFacade() {
    }

    /**
     * Returns list of all repositories fetched from GitHub by calling its REST API
     * @param ownerName short string name of the OWNER from url: https://api.github.com/orgs/OWNER/repos
     * @return List of GitHubRepository
     */
    public List<GitHubRepository> getAllRepositories(String ownerName) {
        GitHubRequest request = GitHubRequestBuilder
                .rest()
                .toURL("https://api.github.com/orgs/" + ownerName + "/repos")
                .fetchPages(50, 1, 1024)
                .build();

        GitHubResponse response = request.execute();

        // GitHubResponse ghResponse = ghCaller.doGetWithAutoPaging("https://api.github.com/orgs/" + ownerName + "/repos", CACHE_TTL_SECONDS);
        if (response.getDataMap() != null) {
            List<Map<String, Object>> listOfMaps = response.getDataMap();

            List<GitHubRepository> result = new ArrayList<>();

            for (Map<String, Object> repoMap : listOfMaps) {
                String repoId   = getStrValue(repoMap, "id");
                String repoName = getStrValue(repoMap, "name");
                String repoUrl  = getStrValue(repoMap, "url");
                String repoCloneUrl = getStrValue(repoMap, "clone_url");

                GitHubRepository ghRepo = new GitHubRepository(repoId, repoName, repoUrl, repoCloneUrl);
                result.add(ghRepo);
            }

            return result;
        }

        return Collections.emptyList();
    }

    /**
     * Returns contributions info for requested repository
     * @param owner String short name of OWNER of repositories
     * @param repositoryName String short name of the repository to request data for
     * @return List of GitHubContributorData
     */
    public List<GitHubContributorData> getContributionsForRepository(String owner, String repositoryName) {
        // GitHubResponse ghResponse = ghCaller.doGet("https://api.github.com/repos/" + owner + "/" + repositoryName +"/contributors", CACHE_TTL_SECONDS);

        GitHubRequest request = GitHubRequestBuilder
                .rest()
                .toURL("https://api.github.com/repos/" + owner + "/" + repositoryName +"/contributors")
                .build();

        GitHubResponse ghResponse = request.execute();

        if (ghResponse.getDataMap() != null) {
            List<Map<String, Object>> listOfMaps = ghResponse.getDataMap();

            List<GitHubContributorData> result = new ArrayList<>();

            for (Map<String, Object> repoMap : listOfMaps) {
                String contributorId   = getStrValue(repoMap, "id");
                String contributorLogin = getStrValue(repoMap, "login");
                int contributionsCount  = getIntValue(repoMap, "contributions");

                GitHubContributorData data = new GitHubContributorData(contributorId, contributorLogin);
                data.setContributionsCount(contributionsCount);
                data.setRepositoryOwner(owner);
                data.setRepositoryName(repositoryName);

                result.add(data);
            }

            return result;
        }

        return Collections.emptyList();
    }

    private static String getStrValue(Map<String, Object> map, String keyName) {
        Object value = map.get(keyName);
        if (value == null) return null;
        return value.toString();
    }

    private static int getIntValue(Map<String, Object> map, String keyName) {
        Object value = map.get(keyName);
        if (value == null) return 0;
        return Integer.parseInt(value.toString());
    }
}
