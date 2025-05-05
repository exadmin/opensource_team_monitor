package com.github.exadmin.ostm.impl;

import com.github.exadmin.ostm.api.collector.ApplicationContext;
import com.github.exadmin.ostm.api.collector.BasicAbstractCollector;
import com.github.exadmin.ostm.api.github.GitHubRESTApiCaller;
import com.github.exadmin.ostm.api.github.GitHubResponse;
import com.github.exadmin.ostm.api.model.TheCellValue;
import com.github.exadmin.ostm.api.model.TheColumn;
import com.github.exadmin.ostm.api.model.TheReportTable;
import com.github.exadmin.ostm.api.model.TheSheet;

import java.util.List;
import java.util.Map;

public class TeamKnownNames extends BasicAbstractCollector {

    @Override
    public void collectDataInto(TheReportTable theReportTable, ApplicationContext applicationContext) {
        GitHubRESTApiCaller ghCaller = new GitHubRESTApiCaller(applicationContext);
        GitHubResponse ghResponse = ghCaller.doGet("https://api.github.com/repos/Netcracker/qubership-core-utils/contributors", 30*60);

        final TheSheet theSheet = theReportTable.getSheet("sheet:team-summary", newSheet -> {
            newSheet.setTitle("Team Summary");
        });

        final TheColumn theColumn = theSheet.getColumn("column:login", newColumn -> {
            newColumn.setTitle("Login Name");
            newColumn.setCssClassName(TheColumn.TD_LEFT_MIDDLE);
        });

        if (ghResponse.getHttpCode() == 200) {
            List<Map<String, Object>> listOfMaps = ghResponse.getDataMap();
            for (Map<String, Object> map : listOfMaps) {
                for (Map.Entry<String, Object> me : map.entrySet()) {
                    if ("login".equals(me.getKey())) {
                        String login = me.getValue().toString();

                        TheCellValue cellValue = new TheCellValue(login);
                        String rowId = "row:" + login;

                        theColumn.addValue(rowId, cellValue);
                    }
                }
            }
        } else {
            getLog().error("Unexpected http code: {}", ghResponse.getHttpCode());
        }
    }
}
