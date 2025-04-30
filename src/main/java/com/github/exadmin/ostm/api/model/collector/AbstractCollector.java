package com.github.exadmin.ostm.api.model.collector;

import com.github.exadmin.ostm.api.model.TheReportModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCollector {
    private Logger log;

    public abstract void collectDataInto(TheReportModel theReportModel, ApplicationContext applicationContext);

    protected Logger getLog() {
        if (log == null) {
            log = LoggerFactory.getLogger(this.getClass());
        }

        return log;
    }
}
