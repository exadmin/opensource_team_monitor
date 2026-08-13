package com.github.exadmin.ostm;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.exadmin.ostm.app.AppSettings;
import com.github.exadmin.ostm.collectors.api.CollectorsFactory;
import com.github.exadmin.ostm.cyberferret.CyberFerretClient;
import com.github.exadmin.ostm.cyberferret.CyberFerretSettings;
import com.github.exadmin.ostm.github.cache.NewCacheManager;
import com.github.exadmin.ostm.persistence.ReportModelPersister;
import com.github.exadmin.ostm.persistence.overrides.JsonReportOverrides;
import com.github.exadmin.ostm.uimodel.GrandReportModel;
import com.github.exadmin.ostm.uimodel.TheReportModel;
import com.github.exadmin.ostm.utils.MiscUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OpenSourceTeamMonitorApp {
    private static final Logger log = LoggerFactory.getLogger(OpenSourceTeamMonitorApp.class);

    public static void main(String[] args) {
        int exitCode;
        try {
            exitCode = run(args);
        } catch (Exception exception) {
            log.error("Open Source Team Monitor could not complete.", exception);
            exitCode = 1;
        }
        if (exitCode != 0) System.exit(exitCode);
    }

    static int run(String[] args) throws Exception {
        if (!log.isDebugEnabled()) {
            System.out.println("Logging debug level is not enabled");
            return 1;
        }
        if (args.length != 6) {
            log.error(usageText());
            return 1;
        }

        String gitHubToken = MiscUtils.getTokenFromArg(args[0]);
        if (gitHubToken == null || gitHubToken.isEmpty()) {
            log.error("GitHub token is required. Provide it through an external file.");
            return 1;
        }
        AppSettings.setGitHubAuthenticationToken(gitHubToken);

        Path repositoriesParent = Paths.get(args[1]);
        if (!repositoriesParent.toFile().isDirectory()) {
            log.error("The cloned-repositories directory was not found: {}", repositoriesParent);
            return 1;
        }

        Path outputFile = Paths.get(args[2]);
        NewCacheManager.setCacheDirectoryPath(args[3]);
        JsonReportOverrides reportOverrides = loadReportOverrides(args[4]);

        CyberFerretSettings cyberFerretSettings;
        try {
            cyberFerretSettings = CyberFerretSettings.from(args[5], System.getenv());
        } catch (IllegalArgumentException exception) {
            log.error("{}\n{}", exception.getMessage(), usageText());
            return 1;
        }
        CyberFerretClient cyberFerretClient = new CyberFerretClient(cyberFerretSettings);
        TheReportModel reportModel = GrandReportModel.getGrandReportInstance();
        reportModel.setReportOverrides(reportOverrides);
        CollectorsFactory collectorsFactory = new CollectorsFactory(
                reportModel,
                repositoriesParent,
                cyberFerretClient);
        collectorsFactory.runCollectors();

        ReportModelPersister reportModelPersister = new ReportModelPersister(reportModel);
        reportModelPersister.saveToFile(outputFile);
        return cyberFerretClient.hasOperationalFailures() ? 2 : 0;
    }

    private static String usageText() {
        return "Usage: OpenSourceTeamMonitorApp " +
                "GITHUB_TOKEN_OR_FILE CLONED_REPOSITORIES_DIRECTORY OUTPUT_REPORT_FILE " +
                "GITHUB_RESPONSE_CACHE_DIRECTORY REPORT_OVERRIDES_FILE CYBER_FERRET_CLI_PATH\n" +
                "\n" +
                "Arguments:\n" +
                "  GITHUB_TOKEN_OR_FILE             File with a GitHub token, or the token value itself.\n" +
                "  CLONED_REPOSITORIES_DIRECTORY    Parent directory where repositories are cloned.\n" +
                "  OUTPUT_REPORT_FILE               File to write the generated report to.\n" +
                "  GITHUB_RESPONSE_CACHE_DIRECTORY  Directory for cached GitHub responses.\n" +
                "  REPORT_OVERRIDES_FILE            JSON file with report overrides.\n" +
                "  CYBER_FERRET_CLI_PATH            Absolute path to a readable cfcli executable.\n" +
                "\n" +
                "Environment:\n" +
                "  CYBER_FERRET_PASSWORD            Required dictionary decryption password.\n" +
                "  CYBER_FERRET_TIMEOUT_SECONDS     Optional positive per-command timeout. Default: 300.";
    }

    private static JsonReportOverrides loadReportOverrides(String fileName) {
        try {
            ObjectMapper mapper = new ObjectMapper(new JsonFactory());
            mapper.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature());
            return mapper.readValue(new File(fileName), JsonReportOverrides.class);
        } catch (Exception exception) {
            log.error("Cannot load the report overrides configuration from {}", fileName, exception);
            return null;
        }
    }
}
