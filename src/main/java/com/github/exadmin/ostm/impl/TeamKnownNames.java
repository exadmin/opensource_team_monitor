package com.github.exadmin.ostm.impl;

import com.github.exadmin.ostm.api.metrics.MetricsFactory;
import com.github.exadmin.ostm.api.model.TheEntity;
import com.github.exadmin.ostm.api.model.TheValue;
import com.github.exadmin.ostm.api.model.categories.CategoriesFactory;
import com.github.exadmin.ostm.api.model.collector.BasicAbstractCollector;
import com.github.exadmin.ostm.api.model.categories.TheCategory;
import com.github.exadmin.ostm.api.metrics.TheMetric;
import com.github.exadmin.ostm.api.model.TheReportModel;

import java.util.List;

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
    public void collectDataInto(TheReportModel theReportModel) {
        List<String> testNames = List.of("name1", "name2", "name3", "name4", "name5");

        for (String name : testNames) {
            TheEntity entity = new TheEntity(name, name);

            theReportModel.addValue(getCategory(), getMetric(), entity, new TheValue(name));
        }
    }
}
