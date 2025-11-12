package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.collectors.api.AbstractOneRepositoryCollector;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.SeverityLevel;
import com.github.exadmin.ostm.uimodel.TheCellValue;
import com.github.exadmin.ostm.uimodel.TheColumn;
import com.github.exadmin.ostm.uimodel.TheReportModel;
import com.github.exadmin.ostm.utils.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AFilesContentChecker extends AbstractOneRepositoryCollector {
    @Override
    protected void processRepository(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path repositoryPath, GitHubRepository repository, TheColumn column) {
        if (column == null) throw new IllegalStateException("Column is null. Was it created in the GrandReportModel using allocateColumn() method?");

        try {
            TheCellValue cellValue = checkOneRepository(repository, gitHubFacade, repositoryPath);
            column.setValue(repository.getId(), cellValue);
        } catch (Exception ex) {
            TheCellValue cellValue = new TheCellValue("Exception", -1, SeverityLevel.ERROR);
            column.setValue(repository.getId(), cellValue);
        }
    }

    protected abstract TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory);

    protected TheCellValue checkOneFileForContent(Path filePath, String httpReference, Pattern ... regExps) {
        File file = filePath.toFile();
        if (!file.exists() || !file.isFile()) {
            return new TheCellValue("Not found", 0, SeverityLevel.ERROR);
        }

        // if no necessity to check over expected content
        if (regExps == null || regExps.length == 0) {
            return new TheCellValue("Ok", 3, SeverityLevel.OK).withHttpReference(httpReference);
        }

        try {
            boolean matches = false;
            for (Pattern regExp : regExps) {
                String fileBody = FileUtils.readFile(filePath);
                Matcher matcher = regExp.matcher(fileBody);
                if (matcher.find()) {
                    matches = true;
                    break;
                }
            }

            if (matches) {
                return new TheCellValue("Ok", 3, SeverityLevel.OK).withHttpReference(httpReference);
            } else {
                return new TheCellValue("Unexpected Content", 2, SeverityLevel.WARN).withHttpReference(httpReference);
            }

        } catch (Exception ex) {
            getLog().error("Error while reading file {}", filePath, ex);
            return new TheCellValue("Internal Error", 1, SeverityLevel.ERROR);
        }
    }

    /**
     * Returns path instance of file which is represented by the arguments.
     * If the last element ends with ".yaml" or ".yml" then attempt to find correspondingly ".yml" or ".yaml" (i.e.another writing") will be done.
     * @param repoDirectory Path instance of the root directory
     * @param more list of string sub-paths (directories and last file)
     * @return
     */
    protected Path findYamlFile(Path repoDirectory, String ... more) {
        Path path = Paths.get(repoDirectory.toString(), more);
        File file = path.toFile();
        if (file.exists() && file.isFile()) return path;

        // support case when yaml-files can be either with "yml" or witl "yaml" extension
        List<String> list = new ArrayList<>(List.of(more));
        String last = list.removeLast();

        if (last.endsWith(".yaml")) {
            last = last.substring(0, last.length() - 5) + ".yml";
        } else if (last.endsWith(".yml")) {
            last = last.substring(0, last.length() - 4) + ".yaml";
        }

        list.add(last);
        return Paths.get(repoDirectory.toString(), list.toArray(new String[0]));
    }
}
