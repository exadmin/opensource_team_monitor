package com.github.exadmin.ostm.persistence;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.exadmin.ostm.uimodel.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ReportModelPersister {
    private static final Logger log = LoggerFactory.getLogger(ReportModelPersister.class);

    private final TheReportModel reportModel;

    public ReportModelPersister(TheReportModel reportModel) {
        this.reportModel = reportModel;
    }

    public void saveToFile(Path outputFilePath) {
        JsonRootContainer rootContainer = new JsonRootContainer();

        for (TheSheet sheet : reportModel.getSheets()) {
            JsonTable jsonTable = new JsonTable(rootContainer);
            jsonTable.setTitle(sheet.getTitle());

            // register metric-columns in the current table
            for (TheColumn theColumn : sheet.getColumns()) {
                JsonColumn jsonColumn = new JsonColumn(jsonTable);
                jsonColumn.setData(theColumn.getId());
                jsonColumn.setTitle(theColumn.getTitle());
                jsonColumn.setClassName(theColumn.getCssClassName());
            }

            // collect all rowidsd

            // add data
            for (String rowId : sheet.getBaseColumn().getRows()) {
                Map<String, Object> dataMap = new HashMap<>();

                for (TheColumn theColumn : sheet.getColumns()) {
                    TheCellValue theCellValue = theColumn.getValue(rowId);
                    if (theCellValue == null) {
                        log.warn("No cell value is registered for sheet '{}', column '{}', rowId '{}'", sheet, theColumn, rowId);
                    }

                    Map<String, String> cellValueJson = new HashMap<>();

                    String visualValue = theCellValue == null ? "null" : theCellValue.getVisualValue();
                    cellValueJson.put("value", visualValue);

                    String techValue   = theCellValue == null ? "null" : theCellValue.getSortByValue();
                    cellValueJson.put("sortByValue", techValue);

                    String toolTip = theCellValue == null ? null : theCellValue.getToolTipText();
                    if (StringUtils.isNotEmpty(toolTip)) cellValueJson.put("title", toolTip);

                    SeverityLevel level = theCellValue == null ? SeverityLevel.ERROR : theCellValue.getSeverityLevel();
                    cellValueJson.put("severity", level.toString());

                    dataMap.put(theColumn.getId(), cellValueJson);
                }

                jsonTable.addDataMap(dataMap);
            }
        }

        try {
            ObjectMapper mapper = new ObjectMapper(new JsonFactory());
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.writeValue(outputFilePath.toFile(), rootContainer);
        } catch (Exception ex) {
            log.error("Error while saving model to json file {}", outputFilePath, ex);
        }
    }
}
