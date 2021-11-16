package com.example.dapr.backend;

public class Counter {

    private int count;

    public int getValue() {
        return count;
    }

    public void setValue(int i) {
        count = i;
    }

    public int increment() {
        count++;
        return count;
    }
}
