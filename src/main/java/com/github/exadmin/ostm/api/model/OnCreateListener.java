package com.github.exadmin.ostm.api.model;

public interface OnCreateListener<T> {
    void process(T newInstance);
}
