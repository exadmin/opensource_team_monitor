package com.github.exadmin.ostm.collectors.impl;

import com.github.exadmin.ostm.collectors.api.AbstractCollector;
import com.github.exadmin.ostm.github.api.GitHubRequest;
import com.github.exadmin.ostm.github.api.GitHubRequestBuilder;
import com.github.exadmin.ostm.github.api.GitHubResponse;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.uimodel.TheCellValue;
import com.github.exadmin.ostm.uimodel.TheColumn;
import com.github.exadmin.ostm.uimodel.TheReportTable;
import com.github.exadmin.ostm.uimodel.TheSheet;

import java.util.List;

public class CountNumberOfCommitsPerUser extends AbstractCollector {
    @Override
    public void collectDataInto(TheReportTable theReportTable, GitHubFacade gitHubFacade) {
        List<String> uniqueLogins = gitHubFacade.getUniqueUsers("Netcracker");

        // create report
        final TheSheet theSheet = theReportTable.findSheet("sheet:team-summary", newSheet -> newSheet.setTitle("Team Summary"));

        final TheColumn theColumn = theSheet.findColumn("column:contributions_count", newColumn -> {
            newColumn.setTitle("Total Contributions Count");
            newColumn.setCssClassName(TheColumn.TD_CENTER_MIDDLE);
            newColumn.setRenderingOrder(-800);
        });

        String gqlQuery = """
                {
                  user(login: "XXXXX") {
                    contributionsCollection(
                      organizationID: "O_kgDOBQbhhA"
                    ) {
                      contributionCalendar {
                        totalContributions
                      }
                    }
                  }
                }""";

        for (String login : uniqueLogins) {
            String newQuery = gqlQuery.replace("XXXXX", login);

            GitHubRequest request = GitHubRequestBuilder
                    .graphQL()
                    .useQuery(newQuery)
                    .build();

            GitHubResponse ghResponse = request.execute();

            Integer count = ghResponse.getObject("data", "user", "contributionsCollection", "contributionCalendar", "totalContributions");
            if (count == null) count = 0;

            TheCellValue cellValue = new TheCellValue("" + count);
            String rowId = "row:" + login;

            theColumn.addValue(rowId, cellValue);
        }
    }
}
