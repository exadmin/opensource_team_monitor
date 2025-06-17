package com.github.exadmin.ostm.collectors.impl.repos.summary;

import com.github.exadmin.ostm.collectors.api.AbstractCollector;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;

import java.nio.file.Path;
import java.util.*;

public class UniqueTeamsCollector extends AbstractCollector {
    private static final String UNDEFINED_STR = "<small>undefined</small>";

    private static final Map<String, String> RED_LEADS_MAP = new HashMap<>();
    static {
        RED_LEADS_MAP.put("qubership-nifi", "Dmitriy Myasnikov");
        RED_LEADS_MAP.put("qubership-integration", "Ivan Sviridov");
        RED_LEADS_MAP.put("qubership-observability", "Ildar Minaev");
        RED_LEADS_MAP.put("qubership-tp", "Denis Arychkov");
        RED_LEADS_MAP.put("qubership-core", "Ilya Smirnov");
        RED_LEADS_MAP.put("qubership-devops", "Pavel Anikin");
        RED_LEADS_MAP.put("qubership-apihub", "Alexander Agishev");
        RED_LEADS_MAP.put("qubership-landscape", "Ilya Smirnov");
        RED_LEADS_MAP.put("qubership-infra", UNDEFINED_STR);
    }

    private static final Map<String, String> BLUE_LEADS_MAP = new HashMap<>();
    static {
        BLUE_LEADS_MAP.put("qubership-nifi", "Sagar Shah");
        BLUE_LEADS_MAP.put("qubership-integration", "Pavels Kletnojs");
        BLUE_LEADS_MAP.put("qubership-observability", "Alexey Karasev");
        BLUE_LEADS_MAP.put("qubership-tp", "Aleksandr Kapustin");
        BLUE_LEADS_MAP.put("qubership-core", "Sergey Lisovoy");
        BLUE_LEADS_MAP.put("qubership-devops", "Mikhail Gushchin");
        BLUE_LEADS_MAP.put("qubership-apihub", "Andrei Chekalin");
        BLUE_LEADS_MAP.put("qubership-landscape", UNDEFINED_STR);
        BLUE_LEADS_MAP.put("qubership-infra", "Sergei I. Ivanov");
    }

    @Override
    public void collectDataInto(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        TheColumn colTeamName = theReportModel.findColumn(TheColumnId.COL_SUMMARY_TEAM_NAME);
        TheColumn colRedLeadName = theReportModel.findColumn(TheColumnId.COL_SUMMARY_TEAM_RED_LEAD_NAME);
        TheColumn colBlueLeadName = theReportModel.findColumn(TheColumnId.COL_SUMMARY_TEAM_BLUE_LEAD_NAME);

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

            String redLeadName = RED_LEADS_MAP.getOrDefault(topic, UNDEFINED_STR);
            colRedLeadName.addValue(topic, new TheCellValue(redLeadName, 0, SeverityLevel.INFO));

            String blueLeadName = BLUE_LEADS_MAP.getOrDefault(topic, UNDEFINED_STR);
            colBlueLeadName.addValue(topic, new TheCellValue(blueLeadName, 0, SeverityLevel.INFO));
        }
    }
}
