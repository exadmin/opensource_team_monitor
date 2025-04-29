package com.github.exadmin.ostm.api.model.collector;

import com.github.exadmin.ostm.api.model.TheReportModel;

public abstract class AbstractCollector {
    public abstract void collectDataInto(TheReportModel theReportModel);
}
