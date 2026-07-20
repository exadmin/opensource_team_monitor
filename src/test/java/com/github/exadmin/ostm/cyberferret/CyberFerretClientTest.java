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
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CyberFerretClientTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void preparesOneSnapshotAndReusesItOffline() throws Exception {
        FakeExecutor executor = new FakeExecutor(result(0, "1.4\n"), result(0, ""));
        CyberFerretClient client = new CyberFerretClient(settings(), executor);
        Path repository = temporaryFolder.newFolder("repository").toPath();

        assertEquals(Optional.of("1.4"), client.dictionaryVersion());
        assertEquals(CyberFerretScanResult.CLEAN, client.scan(repository));

        Invocation metadata = executor.invocations.get(0);
        Invocation scan = executor.invocations.get(1);
        assertTrue(metadata.command().contains("--dictionary-version"));
        assertFalse(metadata.mergeErrorStream());
        assertTrue(scan.command().contains("--mode=quick"));
        assertTrue(scan.command().contains("--offline"));
        assertTrue(scan.mergeErrorStream());
        assertEquals(metadata.workingDirectory(), scan.workingDirectory());
        assertEquals(metadata.command().stream().filter(value -> value.startsWith("--cache-dir=")).findFirst(),
                scan.command().stream().filter(value -> value.startsWith("--cache-dir=")).findFirst());
    }

    @Test
    public void mapsExitCodesToTypedResults() throws Exception {
        FakeExecutor executor = new FakeExecutor(
                result(0, "1.4\n"),
                result(1, "Findings detected\n"),
                result(2, "Scan failed\n"),
                result(9, "Unexpected\n"));
        CyberFerretClient client = new CyberFerretClient(settings(), executor);
        Path repository = temporaryFolder.newFolder("repository").toPath();

        client.dictionaryVersion();
        assertEquals(CyberFerretScanResult.FINDINGS, client.scan(repository));
        assertEquals(CyberFerretScanResult.FAILED, client.scan(repository));
        assertEquals(CyberFerretScanResult.FAILED, client.scan(repository));
        assertTrue(client.hasOperationalFailures());
    }

    @Test
    public void invalidMetadataRemainsAnOperationalFailure() throws Exception {
        FakeExecutor executor = new FakeExecutor(result(0, "unsafe version\n"), result(0, ""));
        CyberFerretClient client = new CyberFerretClient(settings(), executor);
        Path repository = temporaryFolder.newFolder("repository").toPath();

        assertEquals(Optional.empty(), client.dictionaryVersion());
        assertEquals(CyberFerretScanResult.CLEAN, client.scan(repository));
        assertTrue(client.hasOperationalFailures());
    }

    private CyberFerretSettings settings() throws Exception {
        Path jar = temporaryFolder.newFile("cyberferret.jar").toPath();
        Path cache = temporaryFolder.newFolder("cache").toPath();
        return new CyberFerretSettings(jar, cache, "secret-password", Duration.ofSeconds(5));
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
