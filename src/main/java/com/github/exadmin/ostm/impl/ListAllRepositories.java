package com.github.exadmin.ostm.impl;

import com.github.exadmin.ostm.api.collector.ApplicationContext;
import com.github.exadmin.ostm.api.collector.BasicAbstractCollector;
import com.github.exadmin.ostm.api.github.GitHubRESTApiCaller;
import com.github.exadmin.ostm.api.github.GitHubResponse;
import com.github.exadmin.ostm.api.model.TheCellValue;
import com.github.exadmin.ostm.api.model.TheColumn;
import com.github.exadmin.ostm.api.model.TheReportTable;
import com.github.exadmin.ostm.api.model.TheSheet;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ListAllRepositories extends BasicAbstractCollector {

    @Override
    public void collectDataInto(TheReportTable theReportTable, ApplicationContext applicationContext) {
        GitHubRESTApiCaller ghCaller = new GitHubRESTApiCaller(applicationContext);
        ghCaller.setAutoPaging(true);
        ghCaller.setItemsPerPage(50);

        final TheSheet theSheet = theReportTable.getSheet("sheet:all-repos", newSheet -> {
            newSheet.setTitle("All Repositories");
        });

        final TheColumn colRepoNumber = theSheet.getColumn("column:number", newColumn -> {
            newColumn.setTitle("Number");
            newColumn.setCssClassName(TheColumn.TD_LEFT_MIDDLE);
            newColumn.setRenderingOrder(0);
        });

        final TheColumn colRepoName = theSheet.getColumn("column:name", newColumn -> {
            newColumn.setTitle("Repository Name");
            newColumn.setCssClassName(TheColumn.TD_LEFT_MIDDLE);
            newColumn.setRenderingOrder(1);
        });

        // fetch all repositories
        GitHubResponse ghResponse = ghCaller.doGetWithAutoPaging("https://api.github.com/orgs/Netcracker/repos", 30 * 60);
        if (ghResponse.getDataMap() != null) {
            List<Map<String, Object>> list = ghResponse.getDataMap();

            for (Map<String, Object> map : list) {
                String foundRepositoryName = map.get("name").toString();
                String rowId = "row:" + foundRepositoryName;

                TheCellValue cellValue = new TheCellValue(foundRepositoryName);
                colRepoName.addValue(rowId, cellValue);
            }

            getLog().info("Data collected, size = {}, data = {}", list.size(), list);
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
