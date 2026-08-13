package com.github.exadmin.ostm.cyberferret;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class CyberFerretClient {
    private static final Logger log = LoggerFactory.getLogger(CyberFerretClient.class);
    private static final int MAX_METADATA_BYTES = 8 * 1024;

    private final CyberFerretSettings settings;
    private final ProcessExecutor executor;
    private boolean operationalFailures;

    public CyberFerretClient(CyberFerretSettings settings) {
        this(settings, new ProcessExecutor());
    }

    CyberFerretClient(CyberFerretSettings settings, ProcessExecutor executor) {
        this.settings = settings;
        this.executor = executor;
    }

    public CyberFerretScanResult scan(Path repository) {
        Path normalizedRepository = repository.toAbsolutePath().normalize();
        List<String> command = List.of(
                settings.cliPath().toString(),
                "--mode=quick",
                normalizedRepository.toString());
        try {
            ProcessResult result = executor.execute(command, normalizedRepository, childEnvironment(), settings.timeout(),
                    true);
            if (result.timedOut() || !result.cleanupComplete()) {
                return scanFailure(normalizedRepository, result.timedOut() ? "timed out" : "cleanup was incomplete",
                        result.stdout());
            }
            return switch (result.exitCode()) {
                case 0 -> CyberFerretScanResult.CLEAN;
                case 2 -> CyberFerretScanResult.FINDINGS;
                case 1, 3 -> scanFailure(normalizedRepository, "reported an operational failure", result.stdout());
                default -> scanFailure(normalizedRepository, "returned an unexpected exit code", result.stdout());
            };
        } catch (IOException exception) {
            return scanFailure(normalizedRepository, "could not start", new byte[0]);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return scanFailure(normalizedRepository, "was interrupted", new byte[0]);
        }
    }

    public boolean hasOperationalFailures() {
        return operationalFailures;
    }

    private CyberFerretScanResult scanFailure(Path repository, String category, byte[] diagnostic) {
        operationalFailures = true;
        String safeDiagnostic = safeDiagnostic(diagnostic);
        if (safeDiagnostic.isEmpty()) log.error("CyberFerret scan for {} {}.", repository.getFileName(), category);
        else log.error("CyberFerret scan for {} {}: {}", repository.getFileName(), category, safeDiagnostic);
        return CyberFerretScanResult.FAILED;
    }

    private String safeDiagnostic(byte[] bytes) {
        int offset = Math.max(0, bytes.length - MAX_METADATA_BYTES);
        String value = new String(bytes, offset, bytes.length - offset, StandardCharsets.UTF_8)
                .replace(settings.password(), "[redacted]")
                .replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "?")
                .trim();
        return value;
    }

    private Map<String, String> childEnvironment() {
        return Map.of("CYBER_FERRET_PASSWORD", settings.password());
    }
}
