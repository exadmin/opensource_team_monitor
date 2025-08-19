package com.github.exadmin.ostm;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.exadmin.ostm.app.AppSettings;
import com.github.exadmin.ostm.collectors.api.CollectorsFactory;
import com.github.exadmin.ostm.github.signatures.AttentionSignaturesManager;
import com.github.exadmin.ostm.github.cache.NewCacheManager;
import com.github.exadmin.ostm.persistence.ReportModelPersister;
import com.github.exadmin.ostm.persistence.overrides.JsonReportOverrides;
import com.github.exadmin.ostm.uimodel.GrandReportModel;
import com.github.exadmin.ostm.uimodel.TheReportModel;
import com.github.exadmin.ostm.utils.MiscUtils;
import com.github.exadmin.sourcesscanner.exclude.ExcludeFileModel;
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
        if (args.length != 8) {
            log.error("Usage: OpenSourceTeamMonitorApp ARGS\n" +
                    "arg1 - file (readonly) with GitHub token to use or 'gph_...' token itself\n" +
                    "arg2 - parent directory where all necessary repositories are cloned into (into personal subfolders)\n" +
                    "arg3 - output file (read-write) to write results into\n" +
                    "arg4 - cache directory (read-write) to store responses from github\n" +
                    "arg5 - encrypted properties file with signatures to detect\n" +
                    "arg6 - password to encrypt properties file\n" +
                    "arg7 - salt to encrypt properties file\n" +
                    "arg8 - path to file with report overrides (json)");
            System.exit(-1);
        }

        final String gitHubToken = MiscUtils.getTokenFromArg(args[ARG1]);
        if (gitHubToken == null || gitHubToken.isEmpty()) {
            log.error("GitHub token is required for the processing. Provide it via external file. Terminating");
            System.exit(-1);
        }
        AppSettings.setGitHubAuthenticationToken(gitHubToken);

        Path reposParentPath = Paths.get(args[ARG2]);
        if (!reposParentPath.toFile().exists() || !reposParentPath.toFile().isDirectory()) {
            log.error("Provided directory with cloned repositories is not found. Current value is {}", reposParentPath);
            System.exit(-1);
        }

        final Path outputFilePath = Paths.get(args[ARG3]);
        NewCacheManager.setCacheDirectoryPath(args[ARG4]);

        String badWordsFile = args[ARG5];
        String password = MiscUtils.getTokenFromArg(args[ARG6]);
        String salt = MiscUtils.getTokenFromArg(args[ARG7]);

        AttentionSignaturesManager.loadExpressionsFrom(badWordsFile, password, salt);

        // load report overrides from external json file
        JsonReportOverrides reportOverrides = null;
        try {
            File jsonFile = new File(args[ARG8]);
            ObjectMapper mapper = new ObjectMapper(new JsonFactory());
            mapper.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature());
            reportOverrides = mapper.readValue(jsonFile, JsonReportOverrides.class);
        } catch (Exception ex) {
            log.error("Can't load report overrides configuration from {}", args[ARG8], ex);
        }

        // Step2: Run collectors
        TheReportModel reportModel = GrandReportModel.getGrandReportInstance();
        reportModel.setReportOverrides(reportOverrides);
        CollectorsFactory colFactory = new CollectorsFactory(reportModel, reposParentPath);
        colFactory.runCollectors();

        // Step3: Persist data
        ReportModelPersister reportModelPersister = new ReportModelPersister(reportModel);
        reportModelPersister.saveToFile(outputFilePath);
    }


}
