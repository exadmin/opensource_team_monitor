package com.github.exadmin.ostm.collectors.impl.teams;

import com.github.exadmin.ostm.collectors.api.AbstractManyRepositoriesCollector;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.OnlyKnownUsers;
import com.github.exadmin.ostm.uimodel.*;

import java.nio.file.Path;
import java.util.List;

public class TeamKnownNames extends AbstractManyRepositoriesCollector {

    @Override
    public void collectDataIntoImpl(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        // List<String> uniqueUsers = gitHubFacade.getUniqueUsers("Netcracker");
        List<String> uniqueUsers = gitHubFacade.getLoginsOfTheTeam();

        // create report
        final TheColumn colLogin = theReportModel.findColumn(TheColumnId.COL_USER_LOGIN);
        final TheColumn colRealName = theReportModel.findColumn(TheColumnId.COL_USER_REAL_NAME);
        final TheColumn colTeam = theReportModel.findColumn(TheColumnId.COL_USER_TEAM);

        for (String login : uniqueUsers) {
            String rowId = "row:" + login;

            TheCellValue cvLogin = new TheCellValue(login, login, SeverityLevel.INFO);
            colLogin.setValue(rowId, cvLogin);

            SeverityLevel severity = SeverityLevel.INFO;
            String realName = gitHubFacade.getRealNameByLogin(login);
            if (realName == null) {
                realName = "---";
                severity = SeverityLevel.WARN;
            }
            TheCellValue cvRealName = new TheCellValue(realName, realName, severity);
            colRealName.setValue(rowId, cvRealName);

            boolean isRedMember = OnlyKnownUsers.getRedUsersOnly().containsKey(login);
            boolean isBlueMember = OnlyKnownUsers.getBlueUsersOnly().containsKey(login);

            String visValue = isRedMember ? "RED" : (isBlueMember ? "BLUE" : "OTHER");
            int sortValue   = isRedMember ? 2 : (isBlueMember ? 1 : 0);

            TheCellValue cvTeam = new TheCellValue(visValue, sortValue, SeverityLevel.INFO);
            colTeam.setValue(rowId, cvTeam);
        }
    }
}
