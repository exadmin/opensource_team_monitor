package com.github.exadmin.ostm.api.model.categories;

import com.github.exadmin.ostm.api.metrics.TheMetric;
import com.github.exadmin.ostm.api.model.TheEntity;
import com.github.exadmin.ostm.api.model.TheValue;
import com.github.exadmin.ostm.api.utils.Map2D;
import com.github.exadmin.ostm.api.utils.Simple2DMap;

import java.util.List;
import java.util.Objects;

public class TheCategory {
    private final String id;
    private String title;
    private Map2D<TheEntity, TheMetric, TheValue> map2D;

    TheCategory(String id) {
        this.id = id;
        this.map2D = new Simple2DMap<>();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TheCategory that = (TheCategory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<TheMetric> getMetrics() {
        return map2D.getAllColumns();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<TheEntity> getEntities() {
        return map2D.getAllRows();
    }

    public void addValue(TheMetric theMetric, TheEntity theEntity, TheValue theValue) {
        this.map2D.putValue(theEntity, theMetric, theValue);
    }

    @Override
    public String toString() {
        return "TheCategory{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

    public TheValue getValue(TheEntity theEntity, TheMetric theMetric) {
        return map2D.getValue(theEntity, theMetric);
    }
}
