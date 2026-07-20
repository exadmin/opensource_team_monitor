package com.github.exadmin.ostm.cyberferret;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

public record CyberFerretSettings(Path cliJar, Path cacheParent, String password, Duration timeout) {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(300);

    public static CyberFerretSettings from(Map<String, String> environment, Path systemTemporaryDirectory) {
        String cliValue = required(environment, "CYBER_FERRET_CLI_PATH", "CyberFerret CLI path is required.");
        Path cliJar = absolutePath(cliValue, "CyberFerret CLI path is invalid.");
        if (!Files.isRegularFile(cliJar) || !Files.isReadable(cliJar)) {
            throw new IllegalArgumentException("CyberFerret CLI is not a readable file.");
        }

        String password = required(environment, "CYBER_FERRET_PASSWORD", "CyberFerret password is required.");
        Path cacheParent = resolveCacheParent(environment.get("CYBER_FERRET_CACHE_DIR"), systemTemporaryDirectory);
        Duration timeout = parseTimeout(environment.get("CYBER_FERRET_TIMEOUT_SECONDS"));
        return new CyberFerretSettings(cliJar, cacheParent, password, timeout);
    }

    private static String required(Map<String, String> environment, String name, String message) {
        String value = environment.get(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
        return value;
    }

    private static Path resolveCacheParent(String configuredValue, Path systemTemporaryDirectory) {
        Path cacheParent = configuredValue == null || configuredValue.isBlank()
                ? systemTemporaryDirectory.toAbsolutePath().normalize()
                : absolutePath(configuredValue, "CyberFerret cache directory is invalid.");
        if (!Files.isDirectory(cacheParent) || !Files.isWritable(cacheParent)) {
            throw new IllegalArgumentException("CyberFerret cache directory is not writable.");
        }
        return cacheParent;
    }

    private static Path absolutePath(String value, String message) {
        try {
            Path path = Path.of(value);
            if (!path.isAbsolute()) throw new IllegalArgumentException(message);
            return path.normalize();
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(message);
        }
    }

    private static Duration parseTimeout(String value) {
        if (value == null || value.isBlank()) return DEFAULT_TIMEOUT;
        try {
            long seconds = Long.parseLong(value);
            if (seconds <= 0) throw new NumberFormatException();
            return Duration.ofSeconds(seconds);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("CyberFerret timeout must be a positive number of seconds.");
        }
    }
}
