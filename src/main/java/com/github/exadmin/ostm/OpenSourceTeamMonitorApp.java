package com.github.exadmin.ostm;

import com.github.exadmin.ostm.collectors.api.CollectorsFactory;
import com.github.exadmin.ostm.github.api.HttpRequestBuilder;
import com.github.exadmin.ostm.github.cache.NewCacheManager;
import com.github.exadmin.ostm.persistence.ReportModelPersister;
import com.github.exadmin.ostm.uimodel.GrandReportModel;
import com.github.exadmin.ostm.uimodel.TheReportModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OpenSourceTeamMonitorApp {
    private static final Logger log = LoggerFactory.getLogger(OpenSourceTeamMonitorApp.class);

    private static final int ARG1 = 0;
    private static final int ARG2 = 1;
    private static final int ARG3 = 2;
    private static final int ARG4 = 3;

    public static void main(String[] args) {
        // Step1: Initiate application
        if (args.length != 4) {
            log.error("Usage: OpenSourceTeamMonitorApp ARGS\n" +
                    "arg1 - file (readonly) with GitHub token to use or 'gph_...' token itself\n" +
                    "arg2 - parent directory where all necessary repositories are cloned into (into personal subfolders)\n" +
                    "arg3 - output file (read-write) to write results into\n" +
                    "arg4 - cache directory (read-write) to store responses from github");
            System.exit(-1);
        }

        final String gitHubToken = getTokenFromArg(args[ARG1]);
        if (gitHubToken == null || gitHubToken.isEmpty()) {
            log.error("GitHub token is required for the processing. Provide it via external file. Terminating");
            System.exit(-1);
        }
        HttpRequestBuilder.setAuthenticationToken(gitHubToken);

        Path reposParentPath = Paths.get(args[ARG2]);
        if (!reposParentPath.toFile().exists() || !reposParentPath.toFile().isDirectory()) {
            log.error("Provided directory with cloned repositories is not found. Current value is {}", reposParentPath);
            System.exit(-1);
        }

        final Path outputFilePath = Paths.get(args[ARG3]);
        NewCacheManager.setCacheDirectoryPath(args[ARG4]);

        // Step2: Run collectors
        TheReportModel reportModel = GrandReportModel.getGrandReportInstance();
        CollectorsFactory colFactory = new CollectorsFactory(reportModel, reposParentPath);
        colFactory.runCollectors();

        // Step3: Persist data
        ReportModelPersister reportModelPersister = new ReportModelPersister(reportModel);
        reportModelPersister.saveToFile(outputFilePath);
    }

    private static String getTokenFromArg(String argumentValue) {
        try {
            if (argumentValue.startsWith("ghp_")) return argumentValue;

            return Files.readString(Paths.get(argumentValue), StandardCharsets.UTF_8);
        } catch (IOException | NullPointerException ex) {
            log.error("Error while loading token from file {}", argumentValue, ex);
        }

        return null;
    }
}
