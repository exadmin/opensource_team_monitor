package com.github.exadmin.ostm.collectors.api;

import com.github.exadmin.ostm.collectors.impl.repos.NumberOfOpenedPullRequests;
import com.github.exadmin.ostm.collectors.impl.repos.SonarCodeCoverage;
import com.github.exadmin.ostm.collectors.impl.repos.TopicAndTeamPerRepository;
import com.github.exadmin.ostm.collectors.impl.teams.CountNumberOfCommitsPerUser;
import com.github.exadmin.ostm.collectors.impl.repos.ListAllRepositories;
import com.github.exadmin.ostm.collectors.impl.teams.NumberOfCommitsPerWeekPerUser;
import com.github.exadmin.ostm.collectors.impl.teams.TeamKnownNames;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.uimodel.TheReportModel;

import java.util.ArrayList;
import java.util.List;

public class CollectorsFactory {
    private final static List<AbstractCollector> collectors = new ArrayList<>();
    static {
        collectors.add(new TeamKnownNames());
        collectors.add(new ListAllRepositories());
        collectors.add(new CountNumberOfCommitsPerUser());
        collectors.add(new NumberOfCommitsPerWeekPerUser());
        collectors.add(new TopicAndTeamPerRepository());
        collectors.add(new SonarCodeCoverage());
        collectors.add(new NumberOfOpenedPullRequests());
    }

    private final TheReportModel theReportModel;
    private final GitHubFacade gitHubFacade;

    public CollectorsFactory(TheReportModel theReportModel) {
        this.theReportModel = theReportModel;
        this.gitHubFacade = new GitHubFacade();
    }

    public void runCollectors() {
        for (AbstractCollector collector : collectors) {
            collector.collectDataInto(theReportModel, gitHubFacade);
        }
    }
}
