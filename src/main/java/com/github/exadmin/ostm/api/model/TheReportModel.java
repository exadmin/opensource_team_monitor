package com.github.exadmin.ostm.api.model;

import com.github.exadmin.ostm.api.model.categories.TheCategory;
import com.github.exadmin.ostm.api.metrics.TheMetric;

import java.util.ArrayList;
import java.util.List;

public class TheReportModel {
    private final List<TheCategory> categories = new ArrayList<>();
    private List<TheCategory> categoriesCache;

    public void addValue(TheCategory theCategory, TheMetric theMetric, TheEntity theEntity, TheValue theValue) {
        if (!categories.contains(theCategory)) {
            categories.add(theCategory);

            categoriesCache = new ArrayList<>(categories);
        }

        theCategory.addValue(theMetric, theEntity, theValue);
    }

    public List<TheCategory> getCategories() {
        return categoriesCache;
    }
}
