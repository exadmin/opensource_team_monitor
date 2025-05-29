package com.github.exadmin.ostm.uimodel;

import java.util.HashMap;
import java.util.Map;

public enum TheColumId {

    COL_USER_LOGIN("column:user_login"),
    COL_USER_REAL_NAME("column:user_real_name"),
    COL_CONTRIBUTIONS_FOR_ALL_TIMES_ID("column:contributions_for_all_times"),
    COL_WEEK_BACK_01_ID("column:week_back_1"),
    COL_WEEK_BACK_02_ID("column:week_back_2"),
    COL_WEEK_BACK_03_ID("column:week_back_3"),
    COL_WEEK_BACK_04_ID("column:week_back_4"),
    COL_WEEK_BACK_05_ID("column:week_back_5"),
    COL_WEEK_BACK_06_ID("column:week_back_6"),
    COL_WEEK_BACK_07_ID("column:week_back_7"),
    COL_WEEK_BACK_08_ID("column:week_back_8"),
    COL_WEEK_BACK_09_ID("column:week_back_9"),
    COL_WEEK_BACK_10_ID("column:week_back_10"),
    COL_WEEK_BACK_11_ID("column:week_back_11"),
    COL_WEEK_BACK_12_ID("column:week_back_12"),

    COL_REPO_NUMBER("column:repo_number"),
    COL_REPO_NAME("column:repo_name"),
    COL_REPO_TOPICS("column:repo_topics"),
    COL_REPO_SONAR_CODE_COVERAGE_METRIC("column:sonar_metrics_code_coverage"),
    COL_REPO_OPENED_PULL_REQUESTS_COUNT("column:opened_pull_requests_count"),
    COL_REPO_LICENSE_FILE("column:license_file"),
    COL_REPO_README_FILE("column:readme_file"),
    COL_REPO_CLA_FILE("column:cla_file"),
    COL_REPO_CODE_OWNERS_FILE("column:code_owners_file"),
    COL_REPO_CONVENTIONAL_COMMITS_ACTION("column:conventional_commits_action"),
    COL_REPO_LINTER("column:super_linter_or_prettier"),
    COL_REPO_LABELER("column:labeler"),
    COL_REPO_LINT_TITLE("column:lint_title"),
    COL_REPO_PROFANITY_ACTION("colummn:profanity_action"),
    COL_REPO_SEC_MAIN_IS_PROTECTED("column:main_branch_is_protected"),
    COL_REPO_SEC_BAD_LINKS_CHECKER("column:bad_links_checker"),
    COL_REPO_SEC_BAD_WORDS_CHECKER("column:bad_words");

    private final String id;

    TheColumId(String id) {
        this.id = id;
        putIntoCache(this);
    }

    public String getId() {
        return id;
    }

    private static Map<String, TheColumId> cache;

    private static void putIntoCache(TheColumId theColumId) {
        if (cache == null) cache = new HashMap<>();

        cache.put(theColumId.getId(), theColumId);
    }

    public static TheColumId findById(String id) {
        return cache.get(id);
    }
}
