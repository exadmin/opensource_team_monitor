package com.github.exadmin.ostm.api.model;

public class TheEntity {
    private final String id;
    private String title;

    public TheEntity(String id) {
        this.id = id;
    }

    public TheEntity(String id, String title) {
        this.id = id;
        this.title = title;
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

    @Override
    public String toString() {
        return "TheEntity{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
