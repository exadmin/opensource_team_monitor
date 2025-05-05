package com.github.exadmin.ostm.api.persistence;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.exadmin.ostm.api.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ReportModelPersister {
    private static final Logger log = LoggerFactory.getLogger(ReportModelPersister.class);

    private final TheReportTable reportModel;

    public ReportModelPersister(TheReportTable reportModel) {
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

            // add data
            for (String rowId : sheet.getRows()) {
                Map<String, Object> dataMap = new HashMap<>();

                for (TheColumn theColumn : sheet.getColumns()) {
                    TheCellValue theCellValue = theColumn.getValue(rowId);
                    dataMap.put(theColumn.getId(), theCellValue.getValue());
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
