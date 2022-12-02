package com.bakuard.flashcards.dal.impl;

import org.h2.api.AggregateFunction;

import java.sql.Types;

public class CountFalse implements AggregateFunction {

    private int result;

    public CountFalse() {

    }

    @Override
    public int getType(int[] ints) {
        return Types.BOOLEAN;
    }

    @Override
    public void add(Object o) {
        if(o instanceof Boolean value && !value) ++result;
    }

    @Override
    public Object getResult() {
        return result;
    }

}
