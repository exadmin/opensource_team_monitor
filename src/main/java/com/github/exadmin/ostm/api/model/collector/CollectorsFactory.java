package com.github.exadmin.ostm.api.model.collector;

import com.github.exadmin.ostm.api.metrics.TheMetric;
import com.github.exadmin.ostm.api.model.TheEntity;
import com.github.exadmin.ostm.api.model.TheReportModel;
import com.github.exadmin.ostm.api.model.TheValue;
import com.github.exadmin.ostm.api.model.categories.TheCategory;
import com.github.exadmin.ostm.impl.TeamKnownNames;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CollectorsFactory {
    private final static List<AbstractCollector> collectors = new ArrayList<>();
    static {
        collectors.add(new TeamKnownNames());
    }

    private TheReportModel theReportModel;

    public CollectorsFactory(TheReportModel theReportModel) {
        this.theReportModel = theReportModel;
    }

    public void runCollectors() {
        for (AbstractCollector collector : collectors) {
            collector.collectDataInto(theReportModel);
        }
    }

    public void saveResults(Path outputFilePath) {
        for (TheCategory theCat : theReportModel.getCategories()) {
            System.out.println("Category " + theCat);

            for (TheEntity entity : theCat.getEntities()) {
                for (TheMetric metric : theCat.getMetrics()) {
                    TheValue value = theCat.getValue(entity, metric);
                    System.out.println("  value = " + value);
                }
            }
        }
    }
}
