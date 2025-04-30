package com.github.exadmin.ostm.api.model.collector;

import com.github.exadmin.ostm.api.model.categories.TheCategory;
import com.github.exadmin.ostm.api.model.metrics.TheMetric;

public abstract class BasicAbstractCollector extends AbstractCollector {
    protected abstract TheCategory getCategory();
    protected abstract TheMetric getMetric();
}
