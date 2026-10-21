package com.github.exadmin.ostm.cyberferret;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CyberFerretClient {
    private static final Logger log = LoggerFactory.getLogger(CyberFerretClient.class);
    private static final int MAX_METADATA_BYTES = 8 * 1024;

    private final CyberFerretSettings settings;
    private final ProcessExecutor executor;
    private boolean operationalFailures;
    private final Map<Path, CyberFerretScanResult> results = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public CyberFerretClient(CyberFerretSettings settings) {
        this(settings, new ProcessExecutor());
    }

    CyberFerretClient(CyberFerretSettings settings, ProcessExecutor executor) {
        this.settings = settings;
        this.executor = executor;
    }

    public synchronized CyberFerretScanResult scan(Path repository) {
        Path normalizedRepository = repository.toAbsolutePath().normalize();
        return results.computeIfAbsent(normalizedRepository, this::scanRepository);
    }

    private CyberFerretScanResult scanRepository(Path normalizedRepository) {
        List<String> command = List.of(
                settings.cliPath().toString(),
                normalizedRepository.toString());
        int[] counts = new int[2];
        boolean[] invalidOutput = {false};
        try {
            ProcessResult result = executor.execute(command, normalizedRepository, childEnvironment(), settings.timeout(),
                    true, line -> {
                        String value = line.strip();
                        if (!value.startsWith("JSON:")) return;
                        try {
                            JsonNode message = mapper.readTree(value.substring(5));
                            if (message == null || !message.isObject()) {
                                invalidOutput[0] = true;
                            } else if ("found".equals(message.path("type").asText())) {
                                String key = message.path("key").asText();
                                if (key.isEmpty()) invalidOutput[0] = true;
                                else counts["EMAIL".equals(key) ? 0 : 1]++;
                            }
                        } catch (IOException exception) {
                            invalidOutput[0] = true;
                        }
                    });
            if (result.timedOut() || !result.cleanupComplete()) {
                return scanFailure(normalizedRepository, result.timedOut() ? "timed out" : "cleanup was incomplete",
                        result.stdout());
            }
            if (invalidOutput[0]) {
                return scanFailure(normalizedRepository, "returned invalid JSON output", new byte[0]);
            }
            return switch (result.exitCode()) {
                case 0, 2 -> new CyberFerretScanResult(CyberFerretScanResult.ScanStatus.COMPLETED, counts[0], counts[1]);
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
