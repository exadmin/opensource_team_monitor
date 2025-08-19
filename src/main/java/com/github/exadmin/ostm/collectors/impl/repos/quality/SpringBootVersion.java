package com.github.exadmin.ostm.collectors.impl.repos.quality;

import com.github.exadmin.ostm.uimodel.TheColumn;
import com.github.exadmin.ostm.uimodel.TheColumnId;
import com.github.exadmin.ostm.uimodel.TheReportModel;

import java.util.List;

public class SpringBootVersion extends QuarkusSpringParentCollector {
    @Override
    protected String getMavenGroupId() {
        return "org.springframework.boot";
    }

    @Override
    protected List<String> getExtraProperties() {
        return null;
    }

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_REPO_QUALITY_SPRING_BOOT_VERSION);
    }
}
