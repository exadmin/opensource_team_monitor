package com.github.exadmin.ostm.collectors.api;

import com.github.exadmin.ostm.collectors.impl.repos.common.ListAllRepositories;
import com.github.exadmin.ostm.collectors.impl.repos.common.TopicAndTeamPerRepository;
import com.github.exadmin.ostm.collectors.impl.repos.devops.*;
import com.github.exadmin.ostm.collectors.impl.repos.quality.*;
import com.github.exadmin.ostm.collectors.impl.repos.security.AttentionSignaturesChecker;
import com.github.exadmin.ostm.collectors.impl.repos.summary.TotalErrorsCounter;
import com.github.exadmin.ostm.collectors.impl.repos.summary.UniqueTeamsCollector;
import com.github.exadmin.ostm.collectors.impl.teams.CountNumberOfCommitsPerUser;
import com.github.exadmin.ostm.collectors.impl.teams.NumberOfCommitsPerWeekPerUser;
import com.github.exadmin.ostm.collectors.impl.teams.TeamKnownNames;
import com.github.exadmin.ostm.git.GitFacade;
import com.github.exadmin.ostm.uimodel.TheReportModel;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CollectorsFactory {
    private final static List<AbstractCollector> COLLECTORS_ORDERED_EXECUTION = new ArrayList<>();
    static {
        COLLECTORS_ORDERED_EXECUTION.add(new TeamKnownNames());
        COLLECTORS_ORDERED_EXECUTION.add(new ListAllRepositories());
        COLLECTORS_ORDERED_EXECUTION.add(new CountNumberOfCommitsPerUser());
        COLLECTORS_ORDERED_EXECUTION.add(new NumberOfCommitsPerWeekPerUser());
        COLLECTORS_ORDERED_EXECUTION.add(new TopicAndTeamPerRepository());
        COLLECTORS_ORDERED_EXECUTION.add(new SonarCodeCoverage());
        COLLECTORS_ORDERED_EXECUTION.add(new NumberOfOpenedPullRequests());
        COLLECTORS_ORDERED_EXECUTION.add(new LicenseFilePresence());
        COLLECTORS_ORDERED_EXECUTION.add(new ReadmeFilePresence());
        COLLECTORS_ORDERED_EXECUTION.add(new CLAFilePresence());
        COLLECTORS_ORDERED_EXECUTION.add(new CodeOwnersChecker());
        COLLECTORS_ORDERED_EXECUTION.add(new AttentionSignaturesChecker());
        COLLECTORS_ORDERED_EXECUTION.add(new ConventionalCommitsActionChecker());
        COLLECTORS_ORDERED_EXECUTION.add(new SuperLinterChecker());
        COLLECTORS_ORDERED_EXECUTION.add(new LabelerActionChecker());
        COLLECTORS_ORDERED_EXECUTION.add(new LintTitleActionChecker());
        COLLECTORS_ORDERED_EXECUTION.add(new ProfanityChecker());
        COLLECTORS_ORDERED_EXECUTION.add(new BadLinksChecker());
        COLLECTORS_ORDERED_EXECUTION.add(new BuildOnCommit());
        COLLECTORS_ORDERED_EXECUTION.add(new UniqueTeamsCollector());

        COLLECTORS_ORDERED_EXECUTION.add(new LanguagePlatformVersionChecker());
        COLLECTORS_ORDERED_EXECUTION.add(new QuarkusVersion());
        COLLECTORS_ORDERED_EXECUTION.add(new SpringBootVersion());
        COLLECTORS_ORDERED_EXECUTION.add(new SpringFrwkVersion());

        COLLECTORS_ORDERED_EXECUTION.add(new TotalErrorsCounter()); // let it be the last
    }

    private final TheReportModel theReportModel;
    private final GitFacade gitFacade;
    private final Path parentPathForClonedRepositories;

    public CollectorsFactory(TheReportModel theReportModel, Path parentPathForClonedRepositories) {
        this.theReportModel = theReportModel;
        this.gitFacade = new GitFacade();
        this.parentPathForClonedRepositories = parentPathForClonedRepositories;
    }

    public void runCollectors() {
        for (AbstractCollector collector : COLLECTORS_ORDERED_EXECUTION) {
            collector.collectDataInto(theReportModel, gitFacade, parentPathForClonedRepositories);
        }
    }
}
