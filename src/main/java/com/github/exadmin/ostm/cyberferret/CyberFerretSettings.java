package com.github.exadmin.ostm.cyberferret;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

public record CyberFerretSettings(Path cliPath, String password, Duration timeout) {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(300);

    public static CyberFerretSettings from(String cliValue, Map<String, String> environment) {
        if (cliValue == null || cliValue.isBlank()) {
            throw new IllegalArgumentException("CyberFerret CLI path is required.");
        }
        Path cliPath = absolutePath(cliValue, "CyberFerret CLI path is invalid.");
        if (!Files.isRegularFile(cliPath) || !Files.isReadable(cliPath)) {
            throw new IllegalArgumentException("CyberFerret CLI is not a readable file.");
        }

        String password = required(environment, "CYBER_FERRET_PASSWORD", "CyberFerret password is required.");
        Duration timeout = parseTimeout(environment.get("CYBER_FERRET_TIMEOUT_SECONDS"));
        return new CyberFerretSettings(cliPath, password, timeout);
    }

    private static String required(Map<String, String> environment, String name, String message) {
        String value = environment.get(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
        return value;
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
            Duration timeout = Duration.ofSeconds(seconds);
            timeout.toMillis();
            return timeout;
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "CyberFerret timeout must be a positive number of seconds within the supported range.");
        }
    }
}
