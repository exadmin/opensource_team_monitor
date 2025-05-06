package com.github.exadmin.ostm.impl;

import com.github.exadmin.ostm.api.collector.BasicAbstractCollector;
import com.github.exadmin.ostm.api.github.GitHubContributorData;
import com.github.exadmin.ostm.api.github.GitHubFacade;
import com.github.exadmin.ostm.api.github.GitHubRepository;
import com.github.exadmin.ostm.api.model.TheCellValue;
import com.github.exadmin.ostm.api.model.TheColumn;
import com.github.exadmin.ostm.api.model.TheReportTable;
import com.github.exadmin.ostm.api.model.TheSheet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeamKnownNames extends BasicAbstractCollector {

    @Override
    public void collectDataInto(TheReportTable theReportTable, GitHubFacade gitHubFacade) {
        Set<String> uniqueLogins = new HashSet<>();

        List<GitHubRepository> allRepositories = gitHubFacade.getAllRepositories("Netcracker");
        for (GitHubRepository ghRepo : allRepositories) {
            List<GitHubContributorData> ghContributionDataList = gitHubFacade.getContributionsForRepository("Netcracker", ghRepo.getName());
            for (GitHubContributorData data: ghContributionDataList) {
                uniqueLogins.add(data.getLogin());
            }
        }

        // create report
        final TheSheet theSheet = theReportTable.getSheet("sheet:team-summary", newSheet -> {
            newSheet.setTitle("Team Summary");
        });

        final TheColumn theColumn = theSheet.getColumn("column:login", newColumn -> {
            newColumn.setTitle("Login Name");
            newColumn.setCssClassName(TheColumn.TD_LEFT_MIDDLE);
        });

        for (String login : uniqueLogins) {
            TheCellValue cellValue = new TheCellValue(login);
            String rowId = "row:" + login;

            theColumn.addValue(rowId, cellValue);
        }
    }
}
