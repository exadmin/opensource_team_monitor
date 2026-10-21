package com.github.exadmin.ostm.collectors.impl.repos.quality;

import com.github.exadmin.ostm.cyberferret.CyberFerretScanResult;
import com.github.exadmin.ostm.uimodel.SeverityLevel;
import com.github.exadmin.ostm.uimodel.TheCellValue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AllowedEmailsCheckerTest {
    @Test
    public void mapsEmailCountsAndFailuresToReportCells() {
        assertCell(new CyberFerretScanResult(CyberFerretScanResult.ScanStatus.COMPLETED, 0, 4),
                "Ok", 0, SeverityLevel.OK);
        assertCell(new CyberFerretScanResult(CyberFerretScanResult.ScanStatus.COMPLETED, 3, 4),
                "3", 3, SeverityLevel.ERROR);
        assertCell(CyberFerretScanResult.FAILED, "Internal error", 1, SeverityLevel.ERROR);
    }

    private static void assertCell(CyberFerretScanResult result, String value, int numericValue, SeverityLevel severity) {
        TheCellValue cell = AllowedEmailsChecker.toCellValue(result);
        assertEquals(value, cell.getVisualValue());
        assertEquals(Integer.toString(numericValue), cell.getSortByValue());
        assertEquals(severity, cell.getSeverityLevel());
    }
}
