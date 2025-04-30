package com.github.exadmin.ostm.impl;

import com.github.exadmin.ostm.api.github.GitHubRESTApiCaller;
import com.github.exadmin.ostm.api.github.GitHubResponse;
import com.github.exadmin.ostm.api.model.TheEntity;
import com.github.exadmin.ostm.api.model.TheReportModel;
import com.github.exadmin.ostm.api.model.TheValue;
import com.github.exadmin.ostm.api.model.categories.CategoriesFactory;
import com.github.exadmin.ostm.api.model.categories.TheCategory;
import com.github.exadmin.ostm.api.model.collector.ApplicationContext;
import com.github.exadmin.ostm.api.model.collector.BasicAbstractCollector;
import com.github.exadmin.ostm.api.model.metrics.MetricsFactory;
import com.github.exadmin.ostm.api.model.metrics.TheMetric;

import java.util.List;
import java.util.Map;

public class TeamKnownNames extends BasicAbstractCollector {
    @Override
    protected TheCategory getCategory() {
        return CategoriesFactory.TEAM_SUMMARY;
    }

    @Override
    protected TheMetric getMetric() {
        return MetricsFactory.GIT_HUB_LOGIN;
    }

    @Override
    public void collectDataInto(TheReportModel theReportModel, ApplicationContext applicationContext) {
        GitHubRESTApiCaller ghCaller = new GitHubRESTApiCaller(applicationContext);
        GitHubResponse ghResponse = ghCaller.doGet("https://api.github.com/repos/Netcracker/qubership-core-utils/contributors", 30*60);

        if (ghResponse.getHttpCode() == 200) {
            List<Map<String, Object>> listOfMaps = ghResponse.getDataMap();
            for (Map<String, Object> map : listOfMaps) {
                for (Map.Entry<String, Object> me : map.entrySet()) {
                    if ("login".equals(me.getKey())) {
                        TheEntity entity = new TheEntity(me.getKey(), me.getValue().toString());
                        theReportModel.addValue(getCategory(), getMetric(), entity, new TheValue(me.getValue().toString()));
                    }
                }
            }
        } else {
            getLog().error("Unexpected http code: {}", ghResponse.getHttpCode());
        }
    }
}
