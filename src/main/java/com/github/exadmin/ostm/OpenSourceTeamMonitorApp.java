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
        if (args.length != 5) {
            log.error("Usage: OpenSourceTeamMonitorApp ARGS\n" +
                    "arg1 - file (readonly) with GitHub token to use or 'gph_...' token itself\n" +
                    "arg2 - parent directory where all necessary repositories are cloned into\n" +
                    "arg3 - output file (read-write) to write results into\n" +
                    "arg4 - cache directory (read-write) to store responses from GitHub\n" +
                    "arg5 - path to file with report overrides (JSON)");
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

        CyberFerretSettings cyberFerretSettings = CyberFerretSettings.from(
                System.getenv(),
                Path.of(System.getProperty("java.io.tmpdir")));
        CyberFerretClient cyberFerretClient = new CyberFerretClient(cyberFerretSettings);
        cyberFerretClient.dictionaryVersion();

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
