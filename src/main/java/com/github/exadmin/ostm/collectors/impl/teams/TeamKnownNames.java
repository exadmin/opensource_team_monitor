package com.github.exadmin.ostm.collectors.impl.teams;

import com.github.exadmin.ostm.collectors.api.AbstractCollector;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.uimodel.*;

import java.nio.file.Path;
import java.util.List;

public class TeamKnownNames extends AbstractCollector {

    @Override
    public void collectDataInto(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        // List<String> uniqueUsers = gitHubFacade.getUniqueUsers("Netcracker");
        List<String> uniqueUsers = gitHubFacade.getLoginsOfTheTeam();

        // create report
        final TheColumn colLogin = theReportModel.findColumn(TheColumnId.COL_USER_LOGIN);
        final TheColumn colRealName = theReportModel.findColumn(TheColumnId.COL_USER_REAL_NAME);

        for (String login : uniqueUsers) {
            String rowId = "row:" + login;

            TheCellValue cvLogin = new TheCellValue(login, login, SeverityLevel.INFO);
            colLogin.addValue(rowId, cvLogin);

            SeverityLevel severity = SeverityLevel.INFO;
            String realName = gitHubFacade.getRealNameByLogin(login);
            if (realName == null) {
                realName = "---";
                severity = SeverityLevel.WARN;
            }
            TheCellValue cvRealName = new TheCellValue(realName, realName, severity);
            colRealName.addValue(rowId, cvRealName);
        }
    }
}
