package com.github.exadmin.ostm.api.collector;

import com.github.exadmin.ostm.api.github.GitHubFacade;
import com.github.exadmin.ostm.api.model.TheReportTable;
import com.github.exadmin.ostm.impl.CountNumberOfCommitsPerUser;
import com.github.exadmin.ostm.impl.ListAllRepositories;
import com.github.exadmin.ostm.impl.TeamKnownNames;

import java.util.ArrayList;
import java.util.List;

public class CollectorsFactory {
    private final static List<AbstractCollector> collectors = new ArrayList<>();
    static {
        collectors.add(new TeamKnownNames());
        collectors.add(new ListAllRepositories());
        collectors.add(new CountNumberOfCommitsPerUser());
    }

    private TheReportTable theReportTable;

    public CollectorsFactory(TheReportTable theReportTable) {
        this.theReportTable = theReportTable;
    }

    public void runCollectors(GitHubFacade gitHubFacade) {
        for (AbstractCollector collector : collectors) {
            collector.collectDataInto(theReportTable, gitHubFacade);
        }
    }
}
