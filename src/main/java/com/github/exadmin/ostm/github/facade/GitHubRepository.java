package com.github.exadmin.ostm.github.facade;

import java.util.List;
import java.util.Map;

import static com.github.exadmin.ostm.utils.MiscUtils.getListValue;
import static com.github.exadmin.ostm.utils.MiscUtils.getStrValue;

public class GitHubRepository {
    private final Map<String, Object> dataMap;

    public GitHubRepository(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }

    public String getId() {
        return getStrValue(dataMap, "id");
    }

    public String getName() {
        return getStrValue(dataMap, "name");
    }

    public String getUrl() {
        return getStrValue(dataMap, "html_url");
    }

    public String getCloneUrl() {
        return getStrValue(dataMap, "clone_url");
    }

    public String getPullsUrl() {
        return getStrValue(dataMap, "pulls_url");
    }

    public List<String> getTopics() {
        return getListValue(dataMap, "topics");
    }

    public String getHttpReferenceToFileInGitHub(String filePathInRepositoryStartingFromRoot) {
        String defBranch = getStrValue(dataMap, "default_branch");
        String url = getStrValue(dataMap, "html_url");
        return url + "/blob/" + defBranch + filePathInRepositoryStartingFromRoot;
    }


}
