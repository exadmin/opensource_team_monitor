package com.github.exadmin.ostm.collectors.impl.repos.quality;

import com.github.exadmin.ostm.uimodel.TheColumn;
import com.github.exadmin.ostm.uimodel.TheColumnId;
import com.github.exadmin.ostm.uimodel.TheReportModel;

import java.util.List;

public class SpringFrwkVersion extends QuarkusSpringParentCollector {
    @Override
    protected String getMavenGroupId() {
        return "org.springframework";
    }

    @Override
    protected List<String> getExtraProperties() {
        return List.of();
    }

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_REPO_QUALITY_SPRING_FRAMEWORK_VERSION);
    }
}
