package com.github.exadmin.ostm.collectors.impl;

import com.github.exadmin.ostm.collectors.api.AbstractCollector;
import com.github.exadmin.ostm.github.facade.GitHubContributorData;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.model.TheCellValue;
import com.github.exadmin.ostm.model.TheColumn;
import com.github.exadmin.ostm.model.TheReportTable;
import com.github.exadmin.ostm.model.TheSheet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeamKnownNames extends AbstractCollector {

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
        final TheSheet theSheet = theReportTable.findSheet("sheet:team-summary", newSheet -> newSheet.setTitle("Team Summary"));

        final TheColumn theColumn = theSheet.findColumn("column:login", newColumn -> {
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
