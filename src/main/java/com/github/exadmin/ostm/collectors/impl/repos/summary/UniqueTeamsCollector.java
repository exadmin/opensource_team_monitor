package com.github.exadmin.ostm.collectors.impl.repos.summary;

import com.github.exadmin.ostm.collectors.api.AbstractCollector;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;

import java.nio.file.Path;
import java.util.*;

public class UniqueTeamsCollector extends AbstractCollector {
    private static final Map<String, String> LEADS_MAP = new HashMap<>();
    static {
        LEADS_MAP.put("qubership-nifi", null);
        LEADS_MAP.put("qubership-integration", null);
        LEADS_MAP.put("qubership-observability", null);
        LEADS_MAP.put("qubership-tp", null);
        LEADS_MAP.put("qubership-core", "Sergey Lisoviy");
        LEADS_MAP.put("qubership-devops", "Roman Parfinenko");
        LEADS_MAP.put("qubership-apihub", "Alexander Agishev");
        LEADS_MAP.put("qubership-landscape", "Ilya Smirnov");
        LEADS_MAP.put("qubership-infra", null);
    }
    private static final String UNDEFINED_STR = "undefined";

    @Override
    public void collectDataInto(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        TheColumn colTeamName = theReportModel.findColumn(TheColumnId.COL_SUMMARY_TEAM_NAME);
        TheColumn colLeadName = theReportModel.findColumn(TheColumnId.COL_SUMMARY_TEAM_LEAD_NAME);

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
            colTeamName.addValue(topic, new TheCellValue(topic, topic, SeverityLevel.INFO));

            String leadName = LEADS_MAP.get(topic);
            if (leadName == null) leadName = UNDEFINED_STR;
            colLeadName.addValue(topic, new TheCellValue(leadName, 0, SeverityLevel.INFO));
        }
    }
}
