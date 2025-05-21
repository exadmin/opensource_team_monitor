package com.github.exadmin.ostm.github.facade;

import java.util.ArrayList;
import java.util.List;

public class GitHubRepository {
    private final String id;
    private final String name;
    private final String url;
    private final String cloneUrl;
    private final List<String> topics;

    public GitHubRepository(String id, String name, String url, String cloneUrl) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.cloneUrl = cloneUrl;
        this.topics = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getCloneUrl() {
        return cloneUrl;
    }

    public List<String> getTopics() {
        return new ArrayList<>(topics);
    }

    public void addTopic(String topic) {
        topics.add(topic);
    }
}
