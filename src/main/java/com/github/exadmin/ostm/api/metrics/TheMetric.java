package com.github.exadmin.ostm.api.metrics;

public class TheMetric {
    private final String id;
    private String title;

    TheMetric(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
