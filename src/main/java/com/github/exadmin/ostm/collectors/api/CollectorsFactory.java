package com.github.exadmin.ostm.collectors.api;

import com.github.exadmin.ostm.collectors.impl.CountNumberOfCommitsPerUser;
import com.github.exadmin.ostm.collectors.impl.ListAllRepositories;
import com.github.exadmin.ostm.collectors.impl.NumberOfCommitsPerWeekPerUser;
import com.github.exadmin.ostm.collectors.impl.TeamKnownNames;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.uimodel.TheReportTable;

import java.util.ArrayList;
import java.util.List;

public class CollectorsFactory {
    private final static List<AbstractCollector> collectors = new ArrayList<>();
    static {
        collectors.add(new TeamKnownNames());
        collectors.add(new ListAllRepositories());
        collectors.add(new CountNumberOfCommitsPerUser());
        collectors.add(new NumberOfCommitsPerWeekPerUser());
    }

    private final TheReportTable theReportTable;
    private final GitHubFacade gitHubFacade;

    public CollectorsFactory(TheReportTable theReportTable) {
        this.theReportTable = theReportTable;
        this.gitHubFacade = new GitHubFacade();
    }

    public void runCollectors() {
        for (AbstractCollector collector : collectors) {
            collector.collectDataInto(theReportTable, gitHubFacade);
        }
    }
}
