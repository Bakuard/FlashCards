package com.bakuard.flashcards.dal.impl;

import org.h2.api.AggregateFunction;

import java.sql.SQLException;
import java.sql.Types;

public class CountFalse implements AggregateFunction {

    private int result;

    public CountFalse() {

    }

    @Override
    public int getType(int[] ints) throws SQLException {
        return Types.BOOLEAN;
    }

    @Override
    public void add(Object o) throws SQLException {
        if(o instanceof Boolean value && !value) ++result;
    }

    @Override
    public Object getResult() throws SQLException {
        return result;
    }

}
