package com.github.exadmin.ostm.utils;

import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.github.exadmin.ostm.app.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class PropsUtils {
    private static final Logger log = LoggerFactory.getLogger(PropsUtils.class);

    public static AppProperties loadFromFile(String propsFileName) {
        try {
            JavaPropsMapper mapper = new JavaPropsMapper();
            return mapper.readValue(new File(propsFileName), AppProperties.class);
        } catch (IOException ex) {
            log.error("Error while loading application properties from {}", propsFileName, ex);
            System.exit(1);
        }

        throw new IllegalStateException("Unexpected application state");
    }
}