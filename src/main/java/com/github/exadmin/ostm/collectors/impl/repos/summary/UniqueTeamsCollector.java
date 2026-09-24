package com.github.exadmin.ostm.collectors.impl.repos.summary;

import com.github.exadmin.ostm.collectors.api.AbstractManyRepositoriesCollector;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;

import java.nio.file.Path;
import java.util.*;

public class UniqueTeamsCollector extends AbstractManyRepositoriesCollector {
    private static final String UNDEFINED_STR = "<small>undefined</small>";

    private static final Map<String, String> RED_LEADS_MAP = new HashMap<>();
    static {
        RED_LEADS_MAP.put("qubership-nifi", "Dmitriy Myasnikov");
        RED_LEADS_MAP.put("qubership-integration", "Andrei Chumak");
        RED_LEADS_MAP.put("qubership-observability", "Denis Filatov");
        RED_LEADS_MAP.put("qubership-tp", "Denis Arychkov");
        RED_LEADS_MAP.put("qubership-core", "Sergei S. Aleksandrov");
        RED_LEADS_MAP.put("qubership-devops", "Pavel Anikin");
        RED_LEADS_MAP.put("qubership-apihub", "Alexander Agishev");
        RED_LEADS_MAP.put("qubership-landscape", "Ilya Smirnov");
        RED_LEADS_MAP.put("qubership-infra", UNDEFINED_STR);
        RED_LEADS_MAP.put("qubership-infra-fork", UNDEFINED_STR);
        RED_LEADS_MAP.put("qubership-generic", "Ilya Smirnov");
        RED_LEADS_MAP.put("qubership-cm", "Evgeniy A. Popov");
        RED_LEADS_MAP.put("qubership-security", "Roman Kichasov");
    }

    private static final Map<String, String> BLUE_LEADS_MAP = new HashMap<>();
    static {
        BLUE_LEADS_MAP.put("qubership-nifi", "Sagar Shah");
        BLUE_LEADS_MAP.put("qubership-integration", "Alena Novikova");
        BLUE_LEADS_MAP.put("qubership-observability", "Alexey Karasev");
        BLUE_LEADS_MAP.put("qubership-tp", "Elena Kurganova");
        BLUE_LEADS_MAP.put("qubership-core", "Sergey Lisovoy");
        BLUE_LEADS_MAP.put("qubership-devops", "Mikhail Gushchin");
        BLUE_LEADS_MAP.put("qubership-apihub", "Alena Novikova");
        BLUE_LEADS_MAP.put("qubership-landscape", UNDEFINED_STR);
        BLUE_LEADS_MAP.put("qubership-infra", "Dmitrii Rabenok");
        BLUE_LEADS_MAP.put("qubership-infra-fork", "Dmitrii Rabenok");
        BLUE_LEADS_MAP.put("qubership-generic", UNDEFINED_STR);
        BLUE_LEADS_MAP.put("qubership-cm", "Mikhail Gushchin");
        RED_LEADS_MAP.put("qubership-security", "Ekaterina Nelayeva");
    }

    private static final String UTF_CHAR_LINK = "\uD83D\uDD17";
    private static final String UTF_CHAR_RED_CIRCLE = "\uD83D\uDD34";
    private static final String UTF_CHAR_BLUE_CIRCLE = "\uD83D\uDD35";

    private static final Map<String, String> SUPPORT_REPO_MAP = new HashMap<>();
    static {
        SUPPORT_REPO_MAP.put("qubership-nifi", "<a href=\"https://github.com/Netcracker/qubership-nifi\">qubership-nifi</a>");
        SUPPORT_REPO_MAP.put("qubership-integration", "<a href=\"https://github.com/Netcracker/qubership-integration-platform\">qubership-integration-platform</a>");
        SUPPORT_REPO_MAP.put("qubership-observability", "<a href=\"https://github.com/Netcracker/qubership-profiler-agent/\">qubership-profiler-agent</a>");
        SUPPORT_REPO_MAP.put("qubership-tp", UNDEFINED_STR);
        SUPPORT_REPO_MAP.put("qubership-core", "<a href=\"https://github.com/Netcracker/qubership-core-infra\">qubership-core-infra</a>");
        SUPPORT_REPO_MAP.put("qubership-devops", UNDEFINED_STR);
        SUPPORT_REPO_MAP.put("qubership-apihub", "<a href=\"https://github.com/Netcracker/qubership-apihub\">qubership-apihub</a>");
        SUPPORT_REPO_MAP.put("qubership-landscape", UNDEFINED_STR);
        SUPPORT_REPO_MAP.put("qubership-infra", UNDEFINED_STR);
        SUPPORT_REPO_MAP.put("qubership-infra-fork", UNDEFINED_STR);
        SUPPORT_REPO_MAP.put("qubership-generic", UNDEFINED_STR);
        SUPPORT_REPO_MAP.put("qubership-cm", "<a href=\"https://github.com/Netcracker/qubership-envgene\">qubership-envgene</a>");
        SUPPORT_REPO_MAP.put("qubership-security", UNDEFINED_STR);
    }

    @Override
    public void collectDataIntoImpl(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        TheColumn colTeamName = theReportModel.findColumn(TheColumnId.COL_SUMMARY_TEAM_NAME);
        TheColumn colRedLeadName = theReportModel.findColumn(TheColumnId.COL_SUMMARY_TEAM_RED_LEAD_NAME);
        TheColumn colBlueLeadName = theReportModel.findColumn(TheColumnId.COL_SUMMARY_TEAM_BLUE_LEAD_NAME);
        TheColumn colStreamSupportRepo = theReportModel.findColumn(TheColumnId.COL_SUMMARY_TEAM_SUPPORT_UMBRELLA_REPO);

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
            colTeamName.setValue(topic, new TheCellValue(topic, topic, SeverityLevel.INFO));

            String redLeadName = getFromMap(RED_LEADS_MAP, topic, UTF_CHAR_RED_CIRCLE, UNDEFINED_STR);
            colRedLeadName.setValue(topic, new TheCellValue(redLeadName, 0, SeverityLevel.INFO));

            String blueLeadName = getFromMap(BLUE_LEADS_MAP, topic, UTF_CHAR_BLUE_CIRCLE, UNDEFINED_STR);
            colBlueLeadName.setValue(topic, new TheCellValue(blueLeadName, 0, SeverityLevel.INFO));

            String supportRepo = getFromMap(SUPPORT_REPO_MAP, topic, UTF_CHAR_LINK, UNDEFINED_STR);
            colStreamSupportRepo.setValue(topic, new TheCellValue(supportRepo, 0, SeverityLevel.INFO));
        }
    }

    /**
     * Returns value from the map using key.
     * I case value is found - then magic prefix is added to the value.
     * Otherwise - default value is returned
     */
    private static String getFromMap(Map<String, String> map, String key, String magicPrefixForRealValue, String defaultValue) {
        String valueFromMap = map.get(key);
        if (!defaultValue.equals(valueFromMap)) return magicPrefixForRealValue + " " + valueFromMap;
        return valueFromMap;
    }
}
