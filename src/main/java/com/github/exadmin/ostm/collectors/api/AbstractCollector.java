package com.github.exadmin.ostm.collectors.api;

import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.uimodel.TheReportModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public abstract class AbstractCollector {
    private Logger log;

    public abstract void collectDataInto(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories);

    protected Logger getLog() {
        if (log == null) {
            log = LoggerFactory.getLogger(this.getClass());
        }

        return log;
    }
}
