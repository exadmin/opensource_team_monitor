package com.github.exadmin.ostm.collectors.impl.repos.quality;

import com.github.exadmin.ostm.collectors.impl.repos.devops.AFilesContentChecker;
import com.github.exadmin.ostm.cyberferret.CyberFerretClient;
import com.github.exadmin.ostm.cyberferret.CyberFerretScanResult;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;

import java.nio.file.Path;

public class AllowedEmailsChecker extends AFilesContentChecker {
    private final CyberFerretClient cyberFerretClient;

    public AllowedEmailsChecker(CyberFerretClient cyberFerretClient) {
        this.cyberFerretClient = cyberFerretClient;
    }

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_EMAIL_IS_GOOD);
    }

    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        return toCellValue(cyberFerretClient.scan(repoDirectory));
    }

    static TheCellValue toCellValue(CyberFerretScanResult result) {
        if (result.status() == CyberFerretScanResult.ScanStatus.FAILED) {
            return new TheCellValue("Internal error", 1, SeverityLevel.ERROR);
        }
        int count = result.emailFindings();
        return count == 0
                ? new TheCellValue("Ok", 0, SeverityLevel.OK)
                : new TheCellValue(Integer.toString(count), count, SeverityLevel.ERROR);
    }
}
