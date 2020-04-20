package com.intel.cedar.service.client.model;

public class ValuePair<T1, T2> {
    public ValuePair() {

    }

    public ValuePair(T1 first, T2 second) {
        _first = first;
        _second = second;
    }

    public T1 first() {
        return _first;
    }

    public T2 second() {
        return _second;
    }

    private T1 _first = null;
    private T2 _second = null;
}
