package com.github.exadmin.ostm.collectors.impl.about;

import com.github.exadmin.ostm.collectors.api.AbstractManyRepositoriesCollector;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.uimodel.SeverityLevel;
import com.github.exadmin.ostm.uimodel.TheCellValue;
import com.github.exadmin.ostm.uimodel.TheColumn;
import com.github.exadmin.ostm.uimodel.TheColumnId;
import com.github.exadmin.ostm.uimodel.TheReportModel;

import java.nio.file.Path;

public class AboutCollector extends AbstractManyRepositoriesCollector {
    private static final String VERSION = "1.1.1";

    @Override
    public void collectDataIntoImpl(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        TheColumn parameterColumn = theReportModel.findColumn(TheColumnId.COL_ABOUT_PARAMETER);
        TheColumn valueColumn = theReportModel.findColumn(TheColumnId.COL_ABOUT_VALUE);

        parameterColumn.setValue("Version", new TheCellValue("Version", SeverityLevel.INFO));
        valueColumn.setValue("Version", new TheCellValue(VERSION, SeverityLevel.INFO));
    }
}
