package com.github.exadmin.ostm.api.model.collector;

import com.github.exadmin.ostm.api.model.TheReportModel;
import com.github.exadmin.ostm.impl.ListAllRepositories;
import com.github.exadmin.ostm.impl.TeamKnownNames;

import java.util.ArrayList;
import java.util.List;

public class CollectorsFactory {
    private final static List<AbstractCollector> collectors = new ArrayList<>();
    static {
        collectors.add(new TeamKnownNames());
        collectors.add(new ListAllRepositories());
    }

    private TheReportModel theReportModel;

    public CollectorsFactory(TheReportModel theReportModel) {
        this.theReportModel = theReportModel;
    }

    public void runCollectors(Context context) {
        for (AbstractCollector collector : collectors) {
            collector.collectDataInto(theReportModel, context);
        }
    }
}
