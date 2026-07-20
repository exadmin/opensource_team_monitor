package com.github.exadmin.ostm.cyberferret;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProcessExecutorTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void capturesExitCodeAndSeparateStreams() throws Exception {
        ProcessResult result = executor().execute(command("streams"), workingDirectory(), Map.of(),
                Duration.ofSeconds(5), false);

        assertEquals(7, result.exitCode());
        assertEquals("standard-output", text(result.stdout()).trim());
        assertEquals("standard-error", text(result.stderr()).trim());
        assertFalse(result.timedOut());
        assertTrue(result.cleanupComplete());
    }

    @Test
    public void retainsOnlyBoundedOutputTail() throws Exception {
        ProcessResult result = executor().execute(command("large"), workingDirectory(), Map.of(),
                Duration.ofSeconds(5), true);

        assertTrue(result.stdout().length <= 64 * 1024);
        assertTrue(text(result.stdout()).endsWith("TAIL_MARKER"));
        assertEquals(0, result.stderr().length);
    }

    @Test
    public void timesOutWithoutWaitingIndefinitely() throws Exception {
        ProcessResult result = executor().execute(command("sleep"), workingDirectory(), Map.of(),
                Duration.ofMillis(100), true);

        assertTrue(result.timedOut());
        assertTrue(result.cleanupComplete());
    }

    private ProcessExecutor executor() {
        return new ProcessExecutor(64 * 1024, Duration.ofSeconds(1), Duration.ofSeconds(1), Duration.ofSeconds(1));
    }

    private Path workingDirectory() {
        return temporaryFolder.getRoot().toPath();
    }

    private static List<String> command(String mode) {
        return List.of(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp",
                System.getProperty("java.class.path"),
                Child.class.getName(),
                mode);
    }

    private static String text(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static final class Child {
        public static void main(String[] args) throws Exception {
            switch (args[0]) {
                case "streams" -> {
                    System.out.println("standard-output");
                    System.err.println("standard-error");
                    System.exit(7);
                }
                case "large" -> {
                    System.out.print("x".repeat(70 * 1024));
                    System.out.print("TAIL_MARKER");
                }
                case "sleep" -> Thread.sleep(Duration.ofMinutes(1));
                default -> throw new IllegalArgumentException("Unknown mode");
            }
        }
    }
}
