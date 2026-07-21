package com.github.exadmin.ostm.collectors.impl.repos.security;

import com.github.exadmin.ostm.cyberferret.CyberFerretScanResult;
import com.github.exadmin.ostm.uimodel.SeverityLevel;
import com.github.exadmin.ostm.uimodel.TheCellValue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AttentionSignaturesCheckerTest {
    @Test
    public void mapsCyberFerretResultsToReportCells() {
        assertCell(CyberFerretScanResult.CLEAN, "Ok", SeverityLevel.OK);
        assertCell(CyberFerretScanResult.FINDINGS, "Warnings found", SeverityLevel.WARN);
        assertCell(CyberFerretScanResult.FAILED, "Internal error", SeverityLevel.ERROR);
    }

    private static void assertCell(CyberFerretScanResult result, String value, SeverityLevel severity) {
        TheCellValue cell = AttentionSignaturesChecker.toCellValue(result);
        assertEquals(value, cell.getVisualValue());
        assertEquals(severity, cell.getSeverityLevel());
    }
}
