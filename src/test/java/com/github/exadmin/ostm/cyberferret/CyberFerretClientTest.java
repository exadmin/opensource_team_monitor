package com.github.exadmin.ostm.cyberferret;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CyberFerretClientTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void runsCfcliOnceInFullMode() throws Exception {
        FakeExecutor executor = new FakeExecutor(result(0, ""));
        CyberFerretClient client = new CyberFerretClient(settings(), executor);
        Path repository = temporaryFolder.newFolder("repository").toPath();

        assertEquals(new CyberFerretScanResult(CyberFerretScanResult.ScanStatus.COMPLETED, 0, 0), client.scan(repository));

        Invocation scan = executor.invocations.get(0);
        assertEquals(2, scan.command().size());
        client.scan(repository.resolve(".").toAbsolutePath());
        assertEquals(1, executor.invocations.size());
        assertTrue(scan.command().contains(repository.toAbsolutePath().normalize().toString()));
        assertEquals(repository.toAbsolutePath().normalize(), scan.workingDirectory());
        assertEquals(Map.of("CYBER_FERRET_PASSWORD", "secret-password"), scan.environment());
        assertTrue(scan.mergeErrorStream());
    }

    @Test
    public void mapsExitCodesToTypedResults() throws Exception {
        FakeExecutor executor = new FakeExecutor(
                result(2, "JSON: {\"type\":\"found\",\"key\":\"EMAIL\"}\n"),
                result(1, "Scan failed\n"),
                result(3, "Dictionary failed\n"),
                result(9, "Unexpected\n"));
        CyberFerretClient client = new CyberFerretClient(settings(), executor);
        Path repository = temporaryFolder.newFolder("repository").toPath();

        assertEquals(1, client.scan(repository).emailFindings());
        assertEquals(CyberFerretScanResult.FAILED, client.scan(temporaryFolder.newFolder().toPath()));
        assertEquals(CyberFerretScanResult.FAILED, client.scan(temporaryFolder.newFolder().toPath()));
        assertEquals(CyberFerretScanResult.FAILED, client.scan(temporaryFolder.newFolder().toPath()));
        assertTrue(client.hasOperationalFailures());
    }

    private CyberFerretSettings settings() throws Exception {
        Path cli = temporaryFolder.newFile("cfcli").toPath();
        return new CyberFerretSettings(cli, "secret-password", Duration.ofSeconds(5));
    }

    @Test
    public void countsMessagesRatherThanUniqueEmailsAndIgnoresOtherMessageTypes() throws Exception {
        String output = """
                Scan started
                JSON: {"type":"found","key":"EMAIL","found":"apiteam@swagger.io","line":9,"file":"pets.json"}
                JSON: {"type":"found","key":"EMAIL","found":"apiteam@swagger.io","line":10,"file":"pets.json"}
                JSON: {"type":"found","key":"PASSWORD"}
                JSON: {"type":"summary","key":"EMAIL"}
                """;
        CyberFerretClient client = new CyberFerretClient(settings(), new FakeExecutor(result(2, output)));
        assertEquals(new CyberFerretScanResult(CyberFerretScanResult.ScanStatus.COMPLETED, 2, 1),
                client.scan(temporaryFolder.getRoot().toPath()));
    }

    @Test
    public void cachesFailuresAndRejectsMalformedJsonAndIncompleteScans() throws Exception {
        FakeExecutor executor = new FakeExecutor(result(2, "JSON: broken"),
                new ProcessResult(2, true, new byte[0], new byte[0], true),
                new ProcessResult(2, false, new byte[0], new byte[0], false));
        CyberFerretClient client = new CyberFerretClient(settings(), executor);
        Path repository = temporaryFolder.newFolder().toPath();
        assertEquals(CyberFerretScanResult.FAILED, client.scan(repository));
        assertEquals(CyberFerretScanResult.FAILED, client.scan(repository));
        assertEquals(1, executor.invocations.size());
        assertEquals(CyberFerretScanResult.FAILED, client.scan(temporaryFolder.newFolder().toPath()));
        assertEquals(CyberFerretScanResult.FAILED, client.scan(temporaryFolder.newFolder().toPath()));
    }

    private static ProcessResult result(int exitCode, String stdout) {
        return new ProcessResult(exitCode, false, stdout.getBytes(StandardCharsets.UTF_8), new byte[0], true);
    }

    private record Invocation(List<String> command, Path workingDirectory, Map<String, String> environment,
                              boolean mergeErrorStream) {
    }

    private static final class FakeExecutor extends ProcessExecutor {
        private final Deque<ProcessResult> results;
        private final List<Invocation> invocations = new ArrayList<>();

        private FakeExecutor(ProcessResult... results) {
            this.results = new ArrayDeque<>(List.of(results));
        }

        @Override
        public ProcessResult execute(List<String> command, Path workingDirectory, Map<String, String> environment,
                                     Duration timeout, boolean mergeErrorStream, java.util.function.Consumer<String> onLine) {
            invocations.add(new Invocation(List.copyOf(command), workingDirectory, Map.copyOf(environment),
                    mergeErrorStream));
            ProcessResult result = results.removeFirst();
            new String(result.stdout(), StandardCharsets.UTF_8).lines().forEach(onLine);
            return result;
        }
    }
}
