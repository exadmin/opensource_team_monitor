package com.github.exadmin.ostm.github.facade;

import com.github.exadmin.ostm.github.api.GitHubRequest;
import com.github.exadmin.ostm.github.api.GitHubResponse;
import com.github.exadmin.ostm.github.api.HttpRequestBuilder;
import com.github.exadmin.ostm.utils.MiscUtils;

import java.time.LocalDate;
import java.util.*;

import static com.github.exadmin.ostm.utils.MiscUtils.getIntValue;
import static com.github.exadmin.ostm.utils.MiscUtils.getStrValue;

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
        GitHubRequest request = HttpRequestBuilder
                .gitHubRESTCall()
                .toURL("https://api.github.com/orgs/" + ownerName + "/repos")
                .fetchPages(50, 1, 1024)
                .build();

        GitHubResponse response = request.execute();

        if (response.getDataMap() != null) {
            List<Map<String, Object>> listOfMaps = response.getDataMap();

            List<GitHubRepository> result = new ArrayList<>();

            for (Map<String, Object> repoMap : listOfMaps) {
                GitHubRepository ghRepo = new GitHubRepository(repoMap);

                // remove k8-conformance as it brings lotof non intresting data
                if (ghRepo.getName().equals("k8s-conformance")) continue;

                result.add(ghRepo);
            }

            return result;
        }

        return Collections.emptyList();
    }

    /**
     * Returns repository meta-data.
     * @param owner String owner name of repository
     * @param repoShortName String repository name to return data for
     * @return GitHubRepository instance or null if nothing is found
     */
    public GitHubRepository getGitHubRepository(String owner, String repoShortName) {
        List<GitHubRepository> allRepositories = getAllRepositories(owner);
        for (GitHubRepository repository : allRepositories) {
            if (repoShortName.equals(repository.getName())) return repository;
        }

        return null;
    }

    /**
     * Returns contributions info for requested repository
     * @param owner String short name of OWNER of repositories
     * @param repositoryName String short name of the repository to request data for
     * @return List of GitHubContributorData
     */
    public List<GitHubContributorData> getContributionsForRepository(String owner, String repositoryName) {
        // GitHubResponse ghResponse = ghCaller.doGet("https://api.github.com/repos/" + owner + "/" + repositoryName +"/contributors", CACHE_TTL_SECONDS);

        GitHubRequest request = HttpRequestBuilder
                .gitHubRESTCall()
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

    /**
     * Returns list of unique logins which were found as contributors to all public repositories for the specified OWNER
     * @param ownerName GitHub owner of repositories, i.e. https://github.com/OWNER/repos
     * @return List of Strings
     */
    public List<String> getUniqueListOfContributors(String ownerName) {
        Set<String> uniqueLogins = new HashSet<>();

        List<GitHubRepository> allRepositories = getAllRepositories(ownerName);
        for (GitHubRepository ghRepo : allRepositories) {
            List<GitHubContributorData> ghContributionDataList = getContributionsForRepository(ownerName, ghRepo.getName());
            for (GitHubContributorData data: ghContributionDataList) {
                if (data.getLogin() != null) {
                    uniqueLogins.add(data.getLogin());
                }
            }
        }

        return new ArrayList<>(uniqueLogins);
    }

    public List<String> getLoginsOfTheTeam() {
        return new ArrayList<>(OnlyKnownUsers.getKnowUsersOnly().keySet());
    }

    public String getRealNameByLogin(String login) {
        String realName = OnlyKnownUsers.getRealNameByLogin(login);
        if (realName == null) throw new IllegalArgumentException("Unknown login " + login);

        return realName;
    }





    private static final String GQL_QUERY_GET_COMMITS_PER_USER =
            """
                    query  {
                      user(login: "USERXXX") {
                        contributionsCollection(
                          from: "FROMXXX"
                          to: "TOXXX"
                        ) {
                          commitContributionsByRepository {
                            repository {
                              name
                              owner {
                                login
                              }
                              url         \s
                            }
                            contributions{
                              totalCount
                            }
                          }
                          pullRequestContributionsByRepository {
                            repository {
                              name
                              owner {
                                login
                              }
                              url
                            }
                            contributions {
                              totalCount
                            }
                          }
                          issueContributionsByRepository {
                            repository {
                              name
                              owner {
                                login
                              }
                              url
                            }
                            contributions {
                              totalCount
                            }
                          }
                        }
                      }
                    }                    
            """;

    public GitHubCommitsForPeriod getNumberOfCommitsForPeriod(String login, LocalDate fromDate, LocalDate toDate) {

        String query = GQL_QUERY_GET_COMMITS_PER_USER;
        query = query.replace("USERXXX", login);
        query = query.replace("FROMXXX", MiscUtils.dateToStr(fromDate));
        query = query.replace("TOXXX", MiscUtils.dateToStr(toDate));

        GitHubRequest request = HttpRequestBuilder.gitHubGraphQLCall().useQuery(query).build();
        GitHubResponse response = request.execute();

        GitHubCommitsForPeriod result = new GitHubCommitsForPeriod(login);

        // commits
        {
            List<Map<String, Object>> listOfMaps = response.getObject("/data/user/contributionsCollection/commitContributionsByRepository");
            if (listOfMaps != null) {
                for (Map<String, Object> map : listOfMaps) {
                    String url = MiscUtils.getValue(map, "/repository/url");
                    Integer count = MiscUtils.getValue(map, "/contributions/totalCount");

                    result.addCommitsCounter(url, count);
                }
            }
        }

        // PRs
        {
            List<Map<String, Object>> listOfMaps = response.getObject("/data/user/contributionsCollection/pullRequestContributionsByRepository");
            if (listOfMaps != null) {
                for (Map<String, Object> map : listOfMaps) {
                    String url = MiscUtils.getValue(map, "/repository/url");
                    Integer count = MiscUtils.getValue(map, "/contributions/totalCount");

                    result.addPRsCounter(url, count);
                }
            }
        }

        // issue reports
        {
            List<Map<String, Object>> listOfMaps = response.getObject("/data/user/contributionsCollection/issueContributionsByRepository");
            if (listOfMaps != null) {
                for (Map<String, Object> map : listOfMaps) {
                    String url = MiscUtils.getValue(map, "/repository/url");
                    Integer count = MiscUtils.getValue(map, "/contributions/totalCount");

                    result.addIssuesCounter(url, count);
                }
            }
        }

        return result;
    }
}
