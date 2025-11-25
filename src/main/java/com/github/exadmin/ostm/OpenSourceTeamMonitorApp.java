package com.github.exadmin.ostm;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.exadmin.ostm.app.AppProperties;
import com.github.exadmin.ostm.collectors.api.CollectorsFactory;
import com.github.exadmin.ostm.github.cache.NewCacheManager;
import com.github.exadmin.ostm.github.signatures.AttentionSignaturesManager;
import com.github.exadmin.ostm.persistence.ReportModelPersister;
import com.github.exadmin.ostm.persistence.overrides.JsonReportOverrides;
import com.github.exadmin.ostm.uimodel.GrandReportModel;
import com.github.exadmin.ostm.uimodel.TheReportModel;
import com.github.exadmin.ostm.utils.MiscUtils;
import com.github.exadmin.ostm.utils.PropsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OpenSourceTeamMonitorApp {
    private static final Logger log = LoggerFactory.getLogger(OpenSourceTeamMonitorApp.class);

    private static final int ARG1 = 0;
    private static final int ARG2 = 1;
    private static final int ARG3 = 2;
    private static final int ARG4 = 3;
    private static final int ARG5 = 4;
    private static final int ARG6 = 5;
    private static final int ARG7 = 6;
    private static final int ARG8 = 7;

    public static void main(String[] args) {
        // Step1: Initiate application
        if (args.length != 1 {
            log.error("Usage: OpenSourceTeamMonitorApp $PATH_TO_PROPERTIES_FILE$\n");
            System.exit(-1);
        }

        AppProperties appProperties = PropsUtils.loadFromFile(args[0]);

        // Step2: Run collectors
        TheReportModel reportModel = GrandReportModel.getGrandReportInstance();

        CollectorsFactory colFactory = new CollectorsFactory(reportModel, reposParentPath);
        colFactory.runCollectors();

        // Step3: Persist data
        ReportModelPersister reportModelPersister = new ReportModelPersister(reportModel);
        reportModelPersister.saveToFile(outputFilePath);
    }


}
