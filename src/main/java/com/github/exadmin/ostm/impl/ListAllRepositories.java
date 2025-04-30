package com.github.exadmin.ostm.impl;

import com.github.exadmin.ostm.api.github.GitHubRESTApiCaller;
import com.github.exadmin.ostm.api.github.GitHubResponse;
import com.github.exadmin.ostm.api.model.metrics.MetricsFactory;
import com.github.exadmin.ostm.api.model.metrics.TheMetric;
import com.github.exadmin.ostm.api.model.TheReportModel;
import com.github.exadmin.ostm.api.model.categories.CategoriesFactory;
import com.github.exadmin.ostm.api.model.categories.TheCategory;
import com.github.exadmin.ostm.api.model.collector.BasicAbstractCollector;
import com.github.exadmin.ostm.api.model.collector.Context;

import java.util.List;
import java.util.Map;

public class ListAllRepositories extends BasicAbstractCollector {
    @Override
    protected TheCategory getCategory() {
        return CategoriesFactory.ALL_REPOSITORIES;
    }

    @Override
    protected TheMetric getMetric() {
        return MetricsFactory.REPOSITORY_NAME;
    }

    @Override
    public void collectDataInto(TheReportModel theReportModel, Context context) {
        GitHubRESTApiCaller ghCaller = new GitHubRESTApiCaller(context.getGitHubToken());
        ghCaller.setAutoPaging(true);
        ghCaller.setItemsPerPage(50);

        GitHubResponse ghResponse = ghCaller.doGetWithAutoPaging("https://api.github.com/orgs/Netcracker/repos", context, 30 * 60);
        if (ghResponse.getDataMap() != null) {
            List<Map<String, Object>> list = ghResponse.getDataMap();

            getLog().info("Data collected, size = {}, data = {}", list.size(), list);
        }
    }
}
