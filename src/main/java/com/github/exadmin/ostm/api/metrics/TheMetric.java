package com.github.exadmin.ostm.api.metrics;

public class TheMetric {
    private final String id;
    private final String title;

    TheMetric(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "TheMetric{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
