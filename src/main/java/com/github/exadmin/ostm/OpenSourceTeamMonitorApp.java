package com.github.exadmin.ostm;

import com.github.exadmin.ostm.collectors.api.CollectorsFactory;
import com.github.exadmin.ostm.github.api.GitHubRequestBuilder;
import com.github.exadmin.ostm.github.cache.NewCacheManager;
import com.github.exadmin.ostm.uimodel.TheReportTable;
import com.github.exadmin.ostm.persistence.ReportModelPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OpenSourceTeamMonitorApp {
    private static final Logger log = LoggerFactory.getLogger(OpenSourceTeamMonitorApp.class);

    public static void main(String[] args) {
        // Step1: Initiate application
        if (args.length != 3) {
            log.error("Usage: OpenSourceTeamMonitorApp ARGS\n" +
                    "arg1 - file (readonly) with GitHub token to use\n" +
                    "arg2 - output file (read-write) to write results into\n" +
                    "arg3 - cache directory (read-write) to store responses from github");
            System.exit(-1);
        }

        final String gitHubToken = getTokenFromFile(args[0]);
        if (gitHubToken == null || gitHubToken.isEmpty()) {
            log.error("GitHub token is required for the processing. Provide it via external file. Terminating");
            System.exit(-1);
        }
        GitHubRequestBuilder.setAuthenticationToken(gitHubToken);

        final Path outputFilePath = Paths.get(args[1]);
        NewCacheManager.setCacheDirectoryPath(args[2]);

        // Step2: Run collectors
        TheReportTable reportModel = new TheReportTable();
        CollectorsFactory colFactory = new CollectorsFactory(reportModel);
        colFactory.runCollectors();

        // Step3: Persist data
        ReportModelPersister reportModelPersister = new ReportModelPersister(reportModel);
        reportModelPersister.saveToFile(outputFilePath);
    }

    private static String getTokenFromFile(String fileName) {
        try {
            return Files.readString(Paths.get(fileName), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            log.error("Error while loading token from file {}", fileName, ex);
        }

        return null;
    }
}
