package com.bakuard.flashcards.model.auth.resource;

public record Param(String key, Object value) {

    public boolean isValueType(Class<?> type) {
        return type.isInstance(value);
    }

    public <T> T valueAs(Class<T> type) {
        return type.cast(value);
    }

}