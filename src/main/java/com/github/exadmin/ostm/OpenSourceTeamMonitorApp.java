package com.github.exadmin.ostm;

import com.github.exadmin.ostm.api.model.TheReportModel;
import com.github.exadmin.ostm.api.model.collector.ApplicationContext;
import com.github.exadmin.ostm.api.model.collector.CollectorsFactory;
import com.github.exadmin.ostm.api.persistence.ReportModelPersister;
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
        // Step1: Initiate applciation
        final ApplicationContext applicationContext = new ApplicationContext();
        if (args.length != 2) {
            log.error("Usage: OpenSourceTeamMonitorApp $FILE_WITH_GITHUB_TOKEN_TO_READ$ $FILE_TO_WRITE_RESULTS_INTO$");
            System.exit(-1);
        }

        final String gitHubToken = getTokenFromFile(args[0]);
        applicationContext.setGitHubToken(gitHubToken);
        if (gitHubToken == null || gitHubToken.isEmpty()) {
            log.error("GitHub token is required for the processing. Provide it via external file. Terminating");
            System.exit(-1);
        }

        // Step2: Prepare cache folder (if not exsits)
        Path cacheFolder = Paths.get("./cache");
        cacheFolder.toFile().mkdirs();
        applicationContext.setCacheDir(cacheFolder);

        final Path outputPath = Paths.get(args[1]);



        TheReportModel reportModel = new TheReportModel();
        CollectorsFactory colFactory = new CollectorsFactory(reportModel);

        // Step2: Run collectors
        colFactory.runCollectors(applicationContext);

        // Step3: Persist data
        ReportModelPersister reportModelPersister = new ReportModelPersister(reportModel);
        reportModelPersister.saveToFile(outputPath);
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
