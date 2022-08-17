package com.bakuard.flashcards.dto.common;

import java.util.Objects;

public class InterpretationRequestResponse {

    private String value;

    public InterpretationRequestResponse() {
    }

    public String getValue() {
        return value;
    }

    public InterpretationRequestResponse setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InterpretationRequestResponse that = (InterpretationRequestResponse) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "InterpretationRequestResponse{" +
                "value='" + value + '\'' +
                '}';
    }

}
