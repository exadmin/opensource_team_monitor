package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.SeverityLevel;
import com.github.exadmin.ostm.uimodel.TheCellValue;
import com.github.exadmin.ostm.uimodel.TheColumn;
import com.github.exadmin.ostm.uimodel.TheColumnId;
import com.github.exadmin.ostm.uimodel.TheReportModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class MavenPublishingChecker extends AFilesContentChecker {
    private static final String CENTRAL_PLUGIN_GROUP_ID = "org.sonatype.central";
    private static final String CENTRAL_PLUGIN_ARTIFACT_ID = "central-publishing-maven-plugin";
    private static final List<String> MAVEN_CENTRAL_URL_MARKERS = List.of(
            "central.sonatype.com",
            "s01.oss.sonatype.org",
            "oss.sonatype.org"
    );
    private static final List<String> MAVEN_CENTRAL_IDS = List.of(
            "central",
            "ossrh",
            "oss.sonatype.org"
    );
    private static final List<String> GITHUB_IDS = List.of(
            "github",
            "github-central"
    );
    private static final String GITHUB_PACKAGES_URL = "https://maven.pkg.github.com/";

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_REPO_MAVEN_CENTRAL);
    }

    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        if (!Files.isDirectory(repoDirectory)) {
            return new TheCellValue("Was not downloaded", SeverityLevel.ERROR);
        }

        List<Path> pomFiles = findPomFiles(repoDirectory);
        if (pomFiles.isEmpty()) {
            return new TheCellValue("Not a Maven", SeverityLevel.SKIP);
        }

        List<PomCheckResult> results = pomFiles.stream()
                .map(MavenPublishingChecker::checkPom)
                .toList();

        PomCheckResult mavenCentralPublishing = results.stream()
                .filter(PomCheckResult::hasMavenCentralPublishing)
                .findFirst()
                .orElse(null);
        if (mavenCentralPublishing != null) {
            return new TheCellValue("Maven Central", SeverityLevel.ERROR)
                    .withHttpReference(toHttpReference(repo, repoDirectory, mavenCentralPublishing.pomPath()));
        }

        PomCheckResult githubCentralPublishing = results.stream()
                .filter(PomCheckResult::hasGithubCentralPublishing)
                .findFirst()
                .orElse(null);
        if (githubCentralPublishing != null) {
            return new TheCellValue("github-central", SeverityLevel.OK)
                    .withHttpReference(toHttpReference(repo, repoDirectory, githubCentralPublishing.pomPath()));
        }

        return new TheCellValue("Exception", SeverityLevel.ERROR)
                .withHttpReference(toHttpReference(repo, repoDirectory, results.getFirst().pomPath()));
    }

    private static List<Path> findPomFiles(Path repoDirectory) {
        try (Stream<Path> stream = Files.walk(repoDirectory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> "pom.xml".equals(path.getFileName().toString()))
                    .filter(path -> !toLowerCaseNormalizedPath(path).contains("/target/"))
                    .toList();
        } catch (IOException ex) {
            throw new IllegalStateException("Error while fetching pom.xml files from " + repoDirectory, ex);
        }
    }

    static PomCheckResult checkPom(Path pomPath) {
        try {
            Document document = readXml(pomPath);
            Element project = document.getDocumentElement();
            boolean hasMavenCentralPublishing = hasMavenCentralRepository(project)
                    || hasPlugin(project, CENTRAL_PLUGIN_GROUP_ID, CENTRAL_PLUGIN_ARTIFACT_ID);
            boolean hasGithubCentralPublishing = hasGithubRepository(project);

            return new PomCheckResult(pomPath, hasMavenCentralPublishing, hasGithubCentralPublishing);
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            throw new IllegalStateException("Error while reading " + pomPath, ex);
        }
    }

    private static Document readXml(Path pomPath) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setNamespaceAware(false);
        Document document = factory.newDocumentBuilder().parse(pomPath.toFile());
        document.getDocumentElement().normalize();
        return document;
    }

    private static boolean hasMavenCentralRepository(Element project) {
        for (Element repository : distributionManagementRepositories(project)) {
            String url = textOfDirectChild(repository, "url");
            if (containsAny(url, MAVEN_CENTRAL_URL_MARKERS)) return true;

            String id = textOfDirectChild(repository, "id");
            if (url == null && equalsAny(id, MAVEN_CENTRAL_IDS)) return true;
        }

        return false;
    }

    private static boolean hasGithubRepository(Element project) {
        for (Element repository : distributionManagementRepositories(project)) {
            String url = textOfDirectChild(repository, "url");
            if (startsWithIgnoreCase(url, GITHUB_PACKAGES_URL)) return true;

            String id = textOfDirectChild(repository, "id");
            if (url == null && equalsAny(id, GITHUB_IDS)) return true;
        }

        return false;
    }

    private static List<Element> distributionManagementRepositories(Element project) {
        List<Element> result = new ArrayList<>();
        for (Element distributionManagement : descendants(project, "distributionManagement")) {
            result.addAll(directChildren(distributionManagement, "repository"));
            result.addAll(directChildren(distributionManagement, "snapshotRepository"));
        }

        return result;
    }

    private static boolean hasPlugin(Element project, String groupId, String artifactId) {
        for (Element plugin : descendants(project, "plugin")) {
            String actualGroupId = textOfDirectChild(plugin, "groupId");
            String actualArtifactId = textOfDirectChild(plugin, "artifactId");

            boolean groupMatches = groupId == null || groupId.equals(actualGroupId);
            if (groupMatches && artifactId.equals(actualArtifactId)) {
                return true;
            }
        }

        return false;
    }

    private static String textOfDirectChild(Element parent, String childName) {
        Element child = directChild(parent, childName);
        return child == null ? null : child.getTextContent().trim();
    }

    private static Element directChild(Element parent, String childName) {
        List<Element> children = directChildren(parent, childName);
        return children.isEmpty() ? null : children.getFirst();
    }

    private static List<Element> directChildren(Element parent, String childName) {
        List<Element> result = new ArrayList<>();
        NodeList childNodes = parent.getChildNodes();
        for (int index = 0; index < childNodes.getLength(); index++) {
            Node node = childNodes.item(index);
            if (node instanceof Element element && childName.equals(element.getNodeName())) {
                result.add(element);
            }
        }

        return result;
    }

    private static List<Element> descendants(Element root, String elementName) {
        List<Element> result = new ArrayList<>();
        NodeList elements = root.getElementsByTagName(elementName);
        for (int index = 0; index < elements.getLength(); index++) {
            result.add((Element) elements.item(index));
        }

        return result;
    }

    private static boolean containsAny(String value, List<String> candidates) {
        if (value == null) return false;

        String lowerCaseValue = value.toLowerCase(Locale.ROOT);
        return candidates.stream().anyMatch(lowerCaseValue::contains);
    }

    private static boolean startsWithIgnoreCase(String value, String prefix) {
        if (value == null) return false;
        return value.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT));
    }

    private static boolean equalsAny(String value, List<String> candidates) {
        if (value == null) return false;

        String lowerCaseValue = value.toLowerCase(Locale.ROOT);
        return candidates.stream().anyMatch(lowerCaseValue::equals);
    }

    private static String toRepositoryPath(Path repoDirectory, Path filePath) {
        return toNormalizedPath(repoDirectory.relativize(filePath));
    }

    private static String toHttpReference(GitHubRepository repo, Path repoDirectory, Path filePath) {
        if (repo == null) return null;
        return repo.getHttpReferenceToFileInGitHub("/" + toRepositoryPath(repoDirectory, filePath));
    }

    private static String toNormalizedPath(Path path) {
        return path.toString().replace('\\', '/');
    }

    private static String toLowerCaseNormalizedPath(Path path) {
        return toNormalizedPath(path).toLowerCase(Locale.ROOT);
    }

    record PomCheckResult(Path pomPath, boolean hasMavenCentralPublishing, boolean hasGithubCentralPublishing) {
    }
}
