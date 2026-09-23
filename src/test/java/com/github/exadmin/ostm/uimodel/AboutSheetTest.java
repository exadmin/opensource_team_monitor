package com.github.exadmin.ostm.uimodel;

import com.github.exadmin.ostm.collectors.impl.about.AboutCollector;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import org.junit.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AboutSheetTest {
    @Test
    public void addsAboutSheetAfterSummaryWithVersionRow() {
        TheReportModel report = GrandReportModel.getGrandReportInstance();
        List<TheSheet> sheets = report.getSheets();

        assertEquals("Summary by teams", sheets.get(sheets.size() - 2).getTitle());
        TheSheet aboutSheet = sheets.get(sheets.size() - 1);
        assertEquals("About", aboutSheet.getTitle());
        assertEquals(List.of("Parameter", "Value"), aboutSheet.getColumns().stream().map(TheColumn::getTitle).toList());
        assertEquals(List.of("20%", "80%"), aboutSheet.getColumns().stream().map(TheColumn::getWidth).toList());

        new AboutCollector().collectDataIntoImpl(report, new GitHubFacade(), Path.of("."));

        assertEquals(List.of("Version"), aboutSheet.getBaseColumn().getRows());
        assertEquals("Version", report.findColumn(TheColumnId.COL_ABOUT_PARAMETER).getValue("Version").getVisualValue());
        assertEquals("1.1.1", report.findColumn(TheColumnId.COL_ABOUT_VALUE).getValue("Version").getVisualValue());
    }
}
