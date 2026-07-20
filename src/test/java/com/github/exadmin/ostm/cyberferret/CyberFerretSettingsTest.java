package com.github.exadmin.ostm.cyberferret;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

public class CyberFerretSettingsTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void parsesRequiredAndOptionalSettings() throws Exception {
        Path jar = temporaryFolder.newFile("cyberferret.jar").toPath();
        Path cache = temporaryFolder.newFolder("cache").toPath();
        Map<String, String> environment = new HashMap<>();
        environment.put("CYBER_FERRET_CLI_PATH", jar.toString());
        environment.put("CYBER_FERRET_PASSWORD", "secret-password");
        environment.put("CYBER_FERRET_CACHE_DIR", cache.toString());
        environment.put("CYBER_FERRET_TIMEOUT_SECONDS", "17");

        CyberFerretSettings settings = CyberFerretSettings.from(environment, temporaryFolder.getRoot().toPath());

        assertEquals(jar.toAbsolutePath().normalize(), settings.cliJar());
        assertEquals(cache.toAbsolutePath().normalize(), settings.cacheParent());
        assertEquals("secret-password", settings.password());
        assertEquals(Duration.ofSeconds(17), settings.timeout());
    }

    @Test
    public void usesProvidedTemporaryDirectoryAndDefaultTimeout() throws Exception {
        Path jar = temporaryFolder.newFile("cyberferret.jar").toPath();
        Path temporaryDirectory = temporaryFolder.newFolder("runtime").toPath();

        CyberFerretSettings settings = CyberFerretSettings.from(Map.of(
                "CYBER_FERRET_CLI_PATH", jar.toString(),
                "CYBER_FERRET_PASSWORD", "password"), temporaryDirectory);

        assertEquals(temporaryDirectory.toAbsolutePath().normalize(), settings.cacheParent());
        assertEquals(Duration.ofSeconds(300), settings.timeout());
    }

    @Test
    public void rejectsUnsafeSettingsWithoutEchoingPassword() throws Exception {
        Path jar = temporaryFolder.newFile("cyberferret.jar").toPath();
        String password = "DO_NOT_ECHO_THIS";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CyberFerretSettings.from(Map.of(
                        "CYBER_FERRET_CLI_PATH", jar.toString(),
                        "CYBER_FERRET_PASSWORD", password,
                        "CYBER_FERRET_TIMEOUT_SECONDS", "0"), temporaryFolder.getRoot().toPath()));

        assertFalse(exception.getMessage().contains(password));
    }

    @Test
    public void rejectsTimeoutThatCannotBeRepresentedInMilliseconds() throws Exception {
        Path jar = temporaryFolder.newFile("cyberferret.jar").toPath();

        assertThrows(IllegalArgumentException.class, () -> CyberFerretSettings.from(Map.of(
                "CYBER_FERRET_CLI_PATH", jar.toString(),
                "CYBER_FERRET_PASSWORD", "password",
                "CYBER_FERRET_TIMEOUT_SECONDS", Long.toString(Long.MAX_VALUE)),
                temporaryFolder.getRoot().toPath()));
    }
}
