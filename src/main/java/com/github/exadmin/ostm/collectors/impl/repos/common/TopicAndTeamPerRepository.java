package com.github.exadmin.ostm.collectors.impl.repos.common;

import com.github.exadmin.ostm.collectors.api.AbstractManyRepositoriesCollector;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.MiscUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.List;

public class TopicAndTeamPerRepository extends AbstractManyRepositoriesCollector {
    @Override
    public void collectDataIntoImpl(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        TheColumn colTopics = theReportModel.findColumn(TheColumnId.COL_REPO_TOPICS);

        List<GitHubRepository> repoList = gitHubFacade.getAllRepositories("Netcracker");
        for (GitHubRepository repository : repoList) {
            String rowId = repository.getId();
            Integer sum = MiscUtils.getCharSum(repository.getTopics().toString());
            TheCellValue cellValue = new TheCellValue(topicsListToStr(repository.getTopics()), sum, SeverityLevel.INFO);
            colTopics.setValue(rowId, cellValue);
        }
    }

    private static String topicsListToStr(List<String> topics) {
        StringBuilder sb = new StringBuilder();
        String qsTeam = null;

        for (Object topicName : topics) {
            if (topicName == null) continue;
            String topicNameStr = topicName.toString();
            if (StringUtils.isEmpty(topicNameStr)) continue;

            String tmpName = topicNameStr.toUpperCase();
            if (tmpName.startsWith("QUBERSHIP-")) {
                qsTeam = topicNameStr;
            } else {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(topicNameStr);
            }
        }

        return "<b>" + qsTeam + "</b><br><small>" + sb + "</small>";
    }
}
