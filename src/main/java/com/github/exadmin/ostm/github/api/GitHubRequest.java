package com.github.exadmin.ostm.github.api;

import org.apache.hc.core5.http.Method;

public class GitHubRequest {
    Method method;
    String url;
    String bodyText;
    String token;
    int itemsPerPage = 50;
    int fromPage = -1;
    int toPage   = -1;

    GitHubRequest() {
    }

    public GitHubResponse execute() {
        GitHubRequestExecutor executor = new GitHubRequestExecutor();
        return executor.execute(this);
    }

    protected GitHubRequest cloneMe()  {
        GitHubRequest copy = new GitHubRequest();

        copy.method         = this.method;
        copy.url            = this.url;
        copy.bodyText       = this.bodyText;
        copy.token          = this.token;
        copy.itemsPerPage   = this.itemsPerPage;
        copy.fromPage       = this.fromPage;
        copy.toPage         = this.toPage;

        return copy;
    }
}
