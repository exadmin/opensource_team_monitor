package com.github.exadmin.ostm.collectors.impl.repos.security;

import com.github.exadmin.ostm.collectors.impl.repos.devops.AFilesContentChecker;
import com.github.exadmin.ostm.cyberferret.CyberFerretClient;
import com.github.exadmin.ostm.cyberferret.CyberFerretScanResult;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.SeverityLevel;
import com.github.exadmin.ostm.uimodel.TheCellValue;
import com.github.exadmin.ostm.uimodel.TheColumn;
import com.github.exadmin.ostm.uimodel.TheColumnId;
import com.github.exadmin.ostm.uimodel.TheReportModel;

import java.nio.file.Path;

public class AttentionSignaturesChecker extends AFilesContentChecker {
    private final CyberFerretClient cyberFerretClient;

    public AttentionSignaturesChecker(CyberFerretClient cyberFerretClient) {
        this.cyberFerretClient = cyberFerretClient;
    }

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        TheColumn column = theReportModel.findColumn(TheColumnId.COL_REPO_SEC_SIGNATURES_CHECKER);
        String title = cyberFerretClient.dictionaryVersion()
                .map(version -> "Attention signatures (v." + version + ")")
                .orElse("Attention signatures (version unavailable)");
        column.setTitle(title);
        return column;
    }

    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        if ("disable".equalsIgnoreCase(System.getenv("BWC"))) {
            return new TheCellValue("Disabled", 0, SeverityLevel.WARN);
        }
        return toCellValue(cyberFerretClient.scan(repoDirectory));
    }

    static TheCellValue toCellValue(CyberFerretScanResult result) {
        return switch (result) {
            case CLEAN -> new TheCellValue("Ok", 0, SeverityLevel.OK);
            case FINDINGS -> new TheCellValue("Warnings found", 1, SeverityLevel.WARN);
            case FAILED -> new TheCellValue("Internal error", 1, SeverityLevel.ERROR);
        };
    }
}
