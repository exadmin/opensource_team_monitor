package com.github.exadmin.ostm.collectors.impl;

import com.github.exadmin.ostm.collectors.api.AbstractCollector;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.model.TheCellValue;
import com.github.exadmin.ostm.model.TheColumn;
import com.github.exadmin.ostm.model.TheReportTable;
import com.github.exadmin.ostm.model.TheSheet;

import java.util.Comparator;
import java.util.List;

public class ListAllRepositories extends AbstractCollector {

    @Override
    public void collectDataInto(TheReportTable theReportTable, GitHubFacade gitHubFacade) {
        final TheSheet theSheet = theReportTable.findSheet("sheet:all-repos", newSheet -> newSheet.setTitle("All Repositories"));

        final TheColumn colRepoNumber = theSheet.findColumn("column:number", newColumn -> {
            newColumn.setTitle("Number");
            newColumn.setCssClassName(TheColumn.TD_LEFT_MIDDLE);
            newColumn.setRenderingOrder(0);
        });

        final TheColumn colRepoName = theSheet.findColumn("column:name", newColumn -> {
            newColumn.setTitle("Repository Name");
            newColumn.setCssClassName(TheColumn.TD_LEFT_MIDDLE);
            newColumn.setRenderingOrder(1);
        });

        List<GitHubRepository> allRepos = gitHubFacade.getAllRepositories("Netcracker");
        for (GitHubRepository nextRepo : allRepos) {
            String rowId = nextRepo.getId();

            TheCellValue cellValue = new TheCellValue(nextRepo.getName());
            colRepoName.addValue(rowId, cellValue);
        }

        // sort repositories and assign line numbers to them
        theSheet.sortBy(colRepoName, Comparator.comparing(v -> v.getValue() == null ? "" : v.getValue().toLowerCase()));

        int number = 1;
        for (String rowId : theSheet.getRows()) {
            TheCellValue cellValue = new TheCellValue("" + number);
            colRepoNumber.addValue(rowId, cellValue);

            number++;
        }
    }
}
