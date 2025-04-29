package com.github.exadmin.ostm.api.utils;

import java.util.List;

/**
 * Realizes 2D Map, i.e. 2D key and 1D value
 * @param <R> Row type
 * @param <C> Column type
 * @param <V> Value type
 */
public interface Map2D<R, C, V> {
    V getValue(R rowKey, C columnKey);
    void putValue(R rowKey, C columnKey, V value);
    List<R> getAllRows();
    List<C> getAllColumns();
}
