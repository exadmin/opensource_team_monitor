package com.github.exadmin.ostm.collectors.api;

import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.SeverityLevel;
import com.github.exadmin.ostm.uimodel.TheCellValue;

final class RepositoryCheckPolicy {
    private static final String MOVED_TO_MONOREPO = "moved-to-monorepo";

    private RepositoryCheckPolicy() {
    }

    static TheCellValue skippedValue(GitHubRepository repository) {
        if (!repository.getTopics().contains(MOVED_TO_MONOREPO)) return null;
        return new TheCellValue("Skipped", SeverityLevel.SKIP)
                .withTooltip("Repository topic: " + MOVED_TO_MONOREPO);
    }
}
