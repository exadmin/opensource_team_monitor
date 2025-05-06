package com.github.exadmin.ostm.api.collector;

import com.github.exadmin.ostm.api.github.GitHubFacade;
import com.github.exadmin.ostm.api.model.TheReportTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCollector {
    private Logger log;

    public abstract void collectDataInto(TheReportTable theReportTable, GitHubFacade gitHubFacade);

    protected Logger getLog() {
        if (log == null) {
            log = LoggerFactory.getLogger(this.getClass());
        }

        return log;
    }
}
