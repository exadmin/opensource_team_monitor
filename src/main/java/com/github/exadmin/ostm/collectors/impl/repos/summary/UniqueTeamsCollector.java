package com.github.exadmin.ostm.collectors.impl.repos.summary;

import com.github.exadmin.ostm.collectors.api.AbstractCollector;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UniqueTeamsCollector extends AbstractCollector {
    @Override
    public void collectDataInto(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        TheColumn column = theReportModel.findColumn(TheColumnId.COL_SUMMARY_TEAM_NAME);

        Set<String> qsTopics = new HashSet<>();
        List<GitHubRepository> repoList = gitHubFacade.getAllRepositories("Netcracker");
        for (GitHubRepository repo : repoList) {
            List<String> topics = repo.getTopics();
            for (String topic : topics) {
                topic = topic.toLowerCase();
                if (topic.startsWith("qubership-")) qsTopics.add(topic);
            }
        }

        for (String topic : qsTopics) {
            column.addValue(topic, new TheCellValue(topic, topic, SeverityLevel.INFO));
        }
    }
}
