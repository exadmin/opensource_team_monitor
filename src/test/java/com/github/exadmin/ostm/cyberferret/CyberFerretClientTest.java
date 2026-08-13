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
    public void runsCfcliInQuickMode() throws Exception {
        FakeExecutor executor = new FakeExecutor(result(0, ""));
        CyberFerretClient client = new CyberFerretClient(settings(), executor);
        Path repository = temporaryFolder.newFolder("repository").toPath();

        assertEquals(CyberFerretScanResult.CLEAN, client.scan(repository));

        Invocation scan = executor.invocations.get(0);
        assertTrue(scan.command().contains("--mode=quick"));
        assertTrue(scan.command().contains(repository.toAbsolutePath().normalize().toString()));
        assertEquals(repository.toAbsolutePath().normalize(), scan.workingDirectory());
        assertEquals(Map.of("CYBER_FERRET_PASSWORD", "secret-password"), scan.environment());
        assertTrue(scan.mergeErrorStream());
    }

    @Test
    public void mapsExitCodesToTypedResults() throws Exception {
        FakeExecutor executor = new FakeExecutor(
                result(2, "Findings detected\n"),
                result(1, "Scan failed\n"),
                result(3, "Dictionary failed\n"),
                result(9, "Unexpected\n"));
        CyberFerretClient client = new CyberFerretClient(settings(), executor);
        Path repository = temporaryFolder.newFolder("repository").toPath();

        assertEquals(CyberFerretScanResult.FINDINGS, client.scan(repository));
        assertEquals(CyberFerretScanResult.FAILED, client.scan(repository));
        assertEquals(CyberFerretScanResult.FAILED, client.scan(repository));
        assertEquals(CyberFerretScanResult.FAILED, client.scan(repository));
        assertTrue(client.hasOperationalFailures());
    }

    private CyberFerretSettings settings() throws Exception {
        Path cli = temporaryFolder.newFile("cfcli").toPath();
        return new CyberFerretSettings(cli, "secret-password", Duration.ofSeconds(5));
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
                                     Duration timeout, boolean mergeErrorStream) {
            invocations.add(new Invocation(List.copyOf(command), workingDirectory, Map.copyOf(environment),
                    mergeErrorStream));
            return results.removeFirst();
        }
    }
}
