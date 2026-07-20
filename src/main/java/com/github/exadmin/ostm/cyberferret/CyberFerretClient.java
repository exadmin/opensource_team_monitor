package com.github.exadmin.ostm.cyberferret;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class CyberFerretClient {
    private static final Logger log = LoggerFactory.getLogger(CyberFerretClient.class);
    private static final Pattern VERSION = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._+\\-]{0,63}");
    private static final int MAX_METADATA_BYTES = 8 * 1024;

    private final CyberFerretSettings settings;
    private final ProcessExecutor executor;
    private final Path runCacheDirectory;
    private boolean metadataAttempted;
    private Optional<String> cachedVersion = Optional.empty();
    private boolean operationalFailures;

    public CyberFerretClient(CyberFerretSettings settings) throws IOException {
        this(settings, new ProcessExecutor());
    }

    CyberFerretClient(CyberFerretSettings settings, ProcessExecutor executor) throws IOException {
        this.settings = settings;
        this.executor = executor;
        runCacheDirectory = Files.createTempDirectory(settings.cacheParent(), "run-").toAbsolutePath().normalize();
    }

    public synchronized Optional<String> dictionaryVersion() {
        if (metadataAttempted) return cachedVersion;
        metadataAttempted = true;
        List<String> command = List.of(
                javaExecutable(),
                "-jar",
                settings.cliJar().toString(),
                "--dictionary-version",
                cacheOption());
        try {
            ProcessResult result = executor.execute(command, runCacheDirectory, childEnvironment(), settings.timeout(),
                    false);
            if (!isSuccessful(result) || result.stderr().length != 0) {
                recordMetadataFailure("command failed", result.stderr());
                return cachedVersion;
            }
            cachedVersion = parseVersion(result.stdout());
            if (cachedVersion.isEmpty()) recordMetadataFailure("invalid output", new byte[0]);
        } catch (IOException exception) {
            recordMetadataFailure("could not start", new byte[0]);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            recordMetadataFailure("was interrupted", new byte[0]);
        }
        return cachedVersion;
    }

    public CyberFerretScanResult scan(Path repository) {
        if (!metadataAttempted) dictionaryVersion();
        Path normalizedRepository = repository.toAbsolutePath().normalize();
        List<String> command = List.of(
                javaExecutable(),
                "-jar",
                settings.cliJar().toString(),
                "--mode=quick",
                "--offline",
                cacheOption(),
                normalizedRepository.toString());
        try {
            ProcessResult result = executor.execute(command, runCacheDirectory, childEnvironment(), settings.timeout(),
                    true);
            if (result.timedOut() || !result.cleanupComplete()) {
                return scanFailure(normalizedRepository, result.timedOut() ? "timed out" : "cleanup was incomplete",
                        result.stdout());
            }
            return switch (result.exitCode()) {
                case 0 -> CyberFerretScanResult.CLEAN;
                case 1 -> CyberFerretScanResult.FINDINGS;
                case 2 -> scanFailure(normalizedRepository, "reported an operational failure", result.stdout());
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

    private Optional<String> parseVersion(byte[] bytes) {
        if (bytes.length == 0 || bytes.length > MAX_METADATA_BYTES) return Optional.empty();
        String output;
        try {
            output = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException exception) {
            return Optional.empty();
        }
        if (!output.endsWith("\n")) return Optional.empty();
        String version = output.substring(0, output.length() - 1);
        if (version.endsWith("\r")) version = version.substring(0, version.length() - 1);
        return VERSION.matcher(version).matches() ? Optional.of(version) : Optional.empty();
    }

    private static boolean isSuccessful(ProcessResult result) {
        return result.exitCode() == 0 && !result.timedOut() && result.cleanupComplete();
    }

    private void recordMetadataFailure(String category, byte[] diagnostic) {
        operationalFailures = true;
        String safeDiagnostic = safeDiagnostic(diagnostic);
        if (safeDiagnostic.isEmpty()) log.error("CyberFerret metadata {}.", category);
        else log.error("CyberFerret metadata {}: {}", category, safeDiagnostic);
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

    private String cacheOption() {
        return "--cache-dir=" + runCacheDirectory;
    }

    private static String javaExecutable() {
        return Path.of(System.getProperty("java.home"), "bin", "java").toString();
    }
}
