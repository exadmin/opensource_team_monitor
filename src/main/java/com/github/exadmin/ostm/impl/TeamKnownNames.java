package com.github.exadmin.ostm.impl;

import com.github.exadmin.ostm.api.collector.ApplicationContext;
import com.github.exadmin.ostm.api.collector.BasicAbstractCollector;
import com.github.exadmin.ostm.api.github.GitHubRESTApiCaller;
import com.github.exadmin.ostm.api.github.GitHubResponse;
import com.github.exadmin.ostm.api.model.TheCellValue;
import com.github.exadmin.ostm.api.model.TheColumn;
import com.github.exadmin.ostm.api.model.TheReportTable;
import com.github.exadmin.ostm.api.model.TheSheet;

import java.util.*;

public class TeamKnownNames extends BasicAbstractCollector {

    @Override
    public void collectDataInto(TheReportTable theReportTable, ApplicationContext applicationContext) {
        GitHubRESTApiCaller ghCaller = new GitHubRESTApiCaller(applicationContext);

        // list all repositories first
        // fetch all repositories
        List<String> allRepositories = new ArrayList<>();
        {
            GitHubResponse ghResponse = ghCaller.doGetWithAutoPaging("https://api.github.com/orgs/Netcracker/repos", 30 * 60);
            if (ghResponse.getDataMap() != null) {
                List<Map<String, Object>> list = ghResponse.getDataMap();

                for (Map<String, Object> map : list) {
                    String foundRepositoryName = map.get("name").toString();
                    allRepositories.add(foundRepositoryName);
                }
            }
        }

        // For each found repository - fetch users who contributed into it
        Set<String> uniqueLogins = new HashSet<>();
        for (String repoName : allRepositories) {
            GitHubResponse ghResponse = ghCaller.doGet("https://api.github.com/repos/Netcracker/" + repoName +"/contributors", 30 * 60);
            if (ghResponse.getHttpCode() == 200) {
                List<Map<String, Object>> listOfMaps = ghResponse.getDataMap();
                for (Map<String, Object> map : listOfMaps) {
                    for (Map.Entry<String, Object> me : map.entrySet()) {
                        if ("login".equals(me.getKey())) {
                            String login = me.getValue().toString();
                            uniqueLogins.add(login);
                        }
                    }
                }
            } else {
                getLog().error("Unexpected http code: {}", ghResponse.getHttpCode());
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
