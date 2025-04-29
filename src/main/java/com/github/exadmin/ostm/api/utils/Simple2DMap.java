package com.github.exadmin.ostm.api.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Simple2DMap<R, C, V> implements Map2D<R, C, V> {
    private final Map<R, Map<C, V>> row2colMap = new HashMap<>();
    private final List<C> allColumns = new ArrayList<>(); // we need this field to keep appearance order
    private final List<R> allRows = new ArrayList<>(); // we need this field to keep appearance order

    @Override
    public V getValue(R rowKey, C columnKey) {
        Map<C, V> col2valMap = row2colMap.get(rowKey);
        return col2valMap.get(columnKey);
    }

    @Override
    public void putValue(R rowKey, C columnKey, V value) {
        Map<C, V> col2valMap = row2colMap.computeIfAbsent(rowKey, k -> new HashMap<>());
        col2valMap.put(columnKey, value);

        if (!allRows.contains(rowKey)) allRows.add(rowKey);
        if (!allColumns.contains(columnKey)) allColumns.add(columnKey);
    }

    @Override
    public List<R> getAllRows() {
        return new ArrayList<>(allRows);
    }

    @Override
    public List<C> getAllColumns() {
        return new ArrayList<>(allColumns);
    }
}
