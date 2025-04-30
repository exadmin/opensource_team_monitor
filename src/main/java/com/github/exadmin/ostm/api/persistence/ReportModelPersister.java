package com.github.exadmin.ostm.api.persistence;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.exadmin.ostm.api.metrics.TheMetric;
import com.github.exadmin.ostm.api.model.TheEntity;
import com.github.exadmin.ostm.api.model.TheReportModel;
import com.github.exadmin.ostm.api.model.TheValue;
import com.github.exadmin.ostm.api.model.categories.TheCategory;
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

        for (TheCategory cat : reportModel.getCategories()) {
            JsonTable jsonTable = new JsonTable(rootContainer);
            jsonTable.setTitle(cat.getTitle());

            // register metric-columns in the current table
            for (TheMetric metric : cat.getMetrics()) {
                JsonColumn jsonColumn = new JsonColumn(jsonTable);
                jsonColumn.setData(metric.getId());
                jsonColumn.setTitle(metric.getTitle());
                jsonColumn.setClassName(jsonColumn.getClassName());
            }

            // add data
            for (TheEntity entity : cat.getEntities()) {
                Map<String, Object> dataMap = new HashMap<>();

                for (TheMetric metric : cat.getMetrics()) {
                    TheValue value = cat.getValue(entity, metric);
                    dataMap.put(metric.getId(), value.getValue());
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
