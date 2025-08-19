package com.github.exadmin.ostm.collectors.impl.repos.quality;

import com.github.exadmin.ostm.uimodel.TheColumn;
import com.github.exadmin.ostm.uimodel.TheColumnId;
import com.github.exadmin.ostm.uimodel.TheReportModel;

import java.util.List;

public class QuarkusVersion extends QuarkusSpringParentCollector {
    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_REPO_QUALITY_QUARKUS_FRAMEWORK_VERSION);
    }

    @Override
    protected String getMavenGroupId() {
        return "io.quarkus";
    }

    @Override
    protected List<String> getExtraProperties() {
        return List.of("quarkus.platform.version", "quarkus.version");
    }
}
