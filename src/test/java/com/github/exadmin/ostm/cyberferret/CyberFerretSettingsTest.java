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
        Path cli = temporaryFolder.newFile("cfcli").toPath();
        Map<String, String> environment = new HashMap<>();
        environment.put("CYBER_FERRET_PASSWORD", "secret-password");
        environment.put("CYBER_FERRET_TIMEOUT_SECONDS", "17");

        CyberFerretSettings settings = CyberFerretSettings.from(cli.toString(), environment);

        assertEquals(cli.toAbsolutePath().normalize(), settings.cliPath());
        assertEquals("secret-password", settings.password());
        assertEquals(Duration.ofSeconds(17), settings.timeout());
    }

    @Test
    public void usesDefaultTimeout() throws Exception {
        Path cli = temporaryFolder.newFile("cfcli").toPath();
        CyberFerretSettings settings = CyberFerretSettings.from(cli.toString(), Map.of(
                "CYBER_FERRET_PASSWORD", "password"));

        assertEquals(Duration.ofSeconds(300), settings.timeout());
    }

    @Test
    public void rejectsUnsafeSettingsWithoutEchoingPassword() throws Exception {
        Path cli = temporaryFolder.newFile("cfcli").toPath();
        String password = "DO_NOT_ECHO_THIS";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CyberFerretSettings.from(cli.toString(), Map.of(
                        "CYBER_FERRET_PASSWORD", password,
                        "CYBER_FERRET_TIMEOUT_SECONDS", "0")));

        assertFalse(exception.getMessage().contains(password));
    }

    @Test
    public void rejectsTimeoutThatCannotBeRepresentedInMilliseconds() throws Exception {
        Path cli = temporaryFolder.newFile("cfcli").toPath();

        assertThrows(IllegalArgumentException.class, () -> CyberFerretSettings.from(cli.toString(), Map.of(
                "CYBER_FERRET_PASSWORD", "password",
                "CYBER_FERRET_TIMEOUT_SECONDS", Long.toString(Long.MAX_VALUE))));
    }
}
