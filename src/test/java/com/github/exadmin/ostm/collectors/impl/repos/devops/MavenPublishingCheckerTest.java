package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.uimodel.SeverityLevel;
import com.github.exadmin.ostm.uimodel.TheCellValue;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MavenPublishingCheckerTest {
    @Test
    public void detectsMavenCentralPublishing() throws Exception {
        Path pom = writePom("""
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>org.qubership</groupId>
                    <artifactId>sample</artifactId>
                    <version>1.0.0</version>
                    <profiles>
                        <profile>
                            <distributionManagement>
                                <repository>
                                    <url>https://central.sonatype.com/api/v1/publisher</url>
                                </repository>
                            </distributionManagement>
                        </profile>
                    </profiles>
                </project>
                """);

        MavenPublishingChecker.PomCheckResult result = MavenPublishingChecker.checkPom(pom);

        assertTrue(result.hasMavenCentralPublishing());
        assertFalse(result.hasGithubCentralPublishing());
    }

    @Test
    public void detectsGithubCentralPublishing() throws Exception {
        Path pom = writePom("""
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>org.qubership</groupId>
                    <artifactId>sample</artifactId>
                    <version>1.0.0</version>
                    <profiles>
                        <profile>
                            <distributionManagement>
                                <repository>
                                    <url>https://maven.pkg.github.com/Netcracker/sample</url>
                                </repository>
                            </distributionManagement>
                        </profile>
                    </profiles>
                </project>
                """);

        MavenPublishingChecker.PomCheckResult result = MavenPublishingChecker.checkPom(pom);

        assertFalse(result.hasMavenCentralPublishing());
        assertTrue(result.hasGithubCentralPublishing());
    }

    @Test
    public void reportsMavenPomWithoutKnownPublishing() throws Exception {
        Path pom = writePom("""
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>org.qubership</groupId>
                    <artifactId>sample</artifactId>
                    <version>1.0.0</version>
                    <distributionManagement>
                        <repository>
                            <name>Central Maven Repository</name>
                        </repository>
                    </distributionManagement>
                </project>
                """);

        MavenPublishingChecker.PomCheckResult result = MavenPublishingChecker.checkPom(pom);

        assertFalse(result.hasMavenCentralPublishing());
        assertFalse(result.hasGithubCentralPublishing());
    }

    @Test
    public void reportsMavenCentralIfAnyPomContainsIt() throws Exception {
        Path repository = Files.createTempDirectory("maven-publishing-checker-repo");
        Files.writeString(repository.resolve("pom.xml"), """
                <project>
                    <profiles>
                        <profile>
                            <distributionManagement>
                                <repository>
                                    <url>https://maven.pkg.github.com/Netcracker/sample</url>
                                </repository>
                            </distributionManagement>
                        </profile>
                    </profiles>
                </project>
                """);
        Path moduleDirectory = Files.createDirectories(repository.resolve("module"));
        Files.writeString(moduleDirectory.resolve("pom.xml"), """
                <project>
                    <profiles>
                        <profile>
                            <distributionManagement>
                                <repository>
                                    <id>central</id>
                                </repository>
                            </distributionManagement>
                        </profile>
                    </profiles>
                </project>
                """);

        assertEquals(SeverityLevel.ERROR, new MavenPublishingChecker()
                .checkOneRepository(null, null, repository)
                .getSeverityLevel());
    }

    @Test
    public void reportsOkIfOnlyGithubCentralPublishingExists() throws Exception {
        Path repository = Files.createTempDirectory("maven-publishing-checker-repo");
        Files.writeString(repository.resolve("pom.xml"), """
                <project>
                    <profiles>
                        <profile>
                            <distributionManagement>
                                <repository>
                                    <url>https://maven.pkg.github.com/Netcracker/sample</url>
                                </repository>
                            </distributionManagement>
                        </profile>
                    </profiles>
                </project>
                """);

        TheCellValue cellValue = new MavenPublishingChecker().checkOneRepository(null, null, repository);

        assertEquals("github-central", cellValue.getVisualValue());
        assertEquals(SeverityLevel.OK, cellValue.getSeverityLevel());
    }

    @Test
    public void reportsNotMavenIfNoPomExists() throws Exception {
        Path repository = Files.createTempDirectory("maven-publishing-checker-repo");

        TheCellValue cellValue = new MavenPublishingChecker().checkOneRepository(null, null, repository);

        assertEquals("Not a Maven", cellValue.getVisualValue());
        assertEquals(SeverityLevel.SKIP, cellValue.getSeverityLevel());
    }

    @Test
    public void reportsExceptionForMavenPomWithoutKnownPublishing() throws Exception {
        Path repository = Files.createTempDirectory("maven-publishing-checker-repo");
        Files.writeString(repository.resolve("pom.xml"), """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>org.qubership</groupId>
                    <artifactId>sample</artifactId>
                    <version>1.0.0</version>
                </project>
                """);

        TheCellValue cellValue = new MavenPublishingChecker().checkOneRepository(null, null, repository);

        assertEquals("Exception", cellValue.getVisualValue());
        assertEquals(SeverityLevel.ERROR, cellValue.getSeverityLevel());
    }

    @Test
    public void detectsMavenCentralPublishingByPlugin() throws Exception {
        Path pom = writePom("""
                <project>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.sonatype.central</groupId>
                                <artifactId>central-publishing-maven-plugin</artifactId>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """);

        MavenPublishingChecker.PomCheckResult result = MavenPublishingChecker.checkPom(pom);

        assertTrue(result.hasMavenCentralPublishing());
        assertFalse(result.hasGithubCentralPublishing());
    }

    @Test
    public void detectsGithubPublishingByIdWhenUrlIsMissing() throws Exception {
        Path pom = writePom("""
                <project>
                    <distributionManagement>
                        <repository>
                            <id>github-central</id>
                        </repository>
                    </distributionManagement>
                </project>
                """);

        MavenPublishingChecker.PomCheckResult result = MavenPublishingChecker.checkPom(pom);

        assertFalse(result.hasMavenCentralPublishing());
        assertTrue(result.hasGithubCentralPublishing());
    }

    private static Path writePom(String content) throws Exception {
        Path directory = Files.createTempDirectory("maven-publishing-checker-test");
        Path pom = directory.resolve("pom.xml");
        Files.writeString(pom, content);
        return pom;
    }
}
