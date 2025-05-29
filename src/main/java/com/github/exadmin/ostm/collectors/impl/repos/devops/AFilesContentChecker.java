package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.collectors.api.AbstractCollector;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AFilesContentChecker extends AbstractCollector {
    @Override
    public final void collectDataInto(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        TheColumn column = getColumnToAddValueInto(theReportModel);
        if (column == null) throw new IllegalStateException("Column is not. Was it created in the GrandReportModel using allocateColumn() method?");

        List<GitHubRepository> allRepos = gitHubFacade.getAllRepositories("Netcracker");
        for (GitHubRepository repo : allRepos) {
            String repoName = repo.getName();
            Path repoDirectory = Paths.get(parentPathForClonedRepositories.toString(), repoName);

            TheCellValue cellValue = checkOneRepository(repo, gitHubFacade, repoDirectory);
            column.addValue(repo.getId(), cellValue);
        }
    }

    protected abstract TheColumn getColumnToAddValueInto(TheReportModel theReportModel);

    protected abstract TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory);

    protected TheCellValue checkOneFileForContent(Path filePath, String httpReference, Pattern regExp) {
        File file = filePath.toFile();
        if (!file.exists() || !file.isFile()) {
            return new TheCellValue("Not found", 0, SeverityLevel.ERROR);
        }

        // if no necessity to check over expected content
        if (regExp == null) {
            return new TheCellValue("Ok", 3, SeverityLevel.OK).withHttpReference(httpReference);
        }

        try {
            String fileBody = FileUtils.readFile(filePath.toString());
            Matcher matcher = regExp.matcher(fileBody);
            if (matcher.find()) {
                return new TheCellValue("Ok", 3, SeverityLevel.OK).withHttpReference(httpReference);
            } else {
                return new TheCellValue("Unexpected Content", 2, SeverityLevel.WARN).withHttpReference(httpReference);
            }
        } catch (Exception ex) {
            getLog().error("Error while reading file {}", filePath, ex);
            return new TheCellValue("Internal Error", 1, SeverityLevel.ERROR);
        }
    }
}
