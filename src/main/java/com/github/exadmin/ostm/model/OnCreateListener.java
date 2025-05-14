package com.github.exadmin.ostm.model;

public interface OnCreateListener<T> {
    void process(T newInstance);
}
