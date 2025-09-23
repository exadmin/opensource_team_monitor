package com.github.exadmin.ostm.uimodel;

import java.util.HashMap;
import java.util.Map;

public enum TheColumnId {

    COL_USER_LOGIN("C001"),
    COL_USER_REAL_NAME("C002"),
    COL_CONTRIBUTIONS_FOR_ALL_TIMES_ID("C003"),
    COL_WEEK_BACK_01_ID("WEEK_1"),
    COL_WEEK_BACK_02_ID("WEEK_2"),
    COL_WEEK_BACK_03_ID("WEEK_3"),
    COL_WEEK_BACK_04_ID("WEEK_4"),
    COL_WEEK_BACK_05_ID("WEEK_5"),
    COL_WEEK_BACK_06_ID("WEEK_6"),
    COL_WEEK_BACK_07_ID("WEEK_7"),
    COL_WEEK_BACK_08_ID("WEEK_8"),
    COL_WEEK_BACK_09_ID("WEEK_9"),
    COL_WEEK_BACK_10_ID("WEEK_10"),
    COL_WEEK_BACK_11_ID("WEEK_11"),
    COL_WEEK_BACK_12_ID("WEEK_12"),

    COL_REPO_NUMBER("C016", false),
    COL_REPO_NAME("C017", false),
    COL_REPO_TOPICS("C018", false),
    COL_REPO_TYPE("C019", false),
    COL_REPO_SONAR_CODE_COVERAGE_METRIC("C020", true),
    COL_REPO_OPENED_PULL_REQUESTS_COUNT("C021", true),
    COL_REPO_LICENSE_FILE("C022", true),
    COL_REPO_README_FILE("C023", true),
    COL_REPO_CLA_FILE("C024", true),
    COL_REPO_CODE_OWNERS_FILE("C025", true),
    COL_REPO_CONVENTIONAL_COMMITS_ACTION("C026", true),
    COL_REPO_LINTER("C027", true),
    COL_REPO_LABELER("C028", true),
    COL_REPO_LINT_TITLE("C029", true),
    COL_REPO_PROFANITY_ACTION("C030", true),
    COL_REPO_SEC_MAIN_IS_PROTECTED("C031", true),
    COL_REPO_SEC_BAD_LINKS_CHECKER("C032", true),
    COL_REPO_SEC_SIGNATURES_CHECKER("C033", true),
    COL_REPO_BUILD_ON_COMMIT("C034", true),
    COL_SUMMARY_TEAM_NAME("C035"),
    COL_SUMMARY_TEAM_TOTAL_ERRORS("C036"),
    COL_SUMMARY_TEAM_TOTAL_REPOSITORIES("C037"),
    COL_SUMMARY_TEAM_ERRS_PER_REPOSITORY("C038"),
    COL_SUMMARY_TEAM_RED_LEAD_NAME("C039"),
    COL_SUMMARY_TEAM_BLUE_LEAD_NAME("C040"),
    COL_REPO_PLATFORM_SDK_VERSION("C041", true),
    COL_REPO_QUALITY_SPRING_FRAMEWORK_VERSION("C042", true),
    COL_REPO_QUALITY_SPRING_BOOT_VERSION("C043", true),
    COL_REPO_QUALITY_QUARKUS_FRAMEWORK_VERSION("C044", true);


    private final String id;
    private final boolean renderIdToAllowOverrideValue;

    TheColumnId(String id, boolean renderIdToAllowOverrideValue) {
        this.id = id;
        this.renderIdToAllowOverrideValue = renderIdToAllowOverrideValue;
        putIntoCache(this);
    }

    TheColumnId(String id) {
        this(id, false);
    }

    public String getId() {
        return id;
    }

    public boolean isRenderIdToAllowOverrideValue() {
        return renderIdToAllowOverrideValue;
    }

    private static Map<String, TheColumnId> cache;

    private static void putIntoCache(TheColumnId theColumId) {
        if (cache == null) cache = new HashMap<>();

        if (cache.containsKey(theColumId.getId())) throw new IllegalStateException("Duplicate column id");

        cache.put(theColumId.getId(), theColumId);
    }

    public static TheColumnId findById(String id) {
        return cache.get(id);
    }
}
