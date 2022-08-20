package com.bakuard.flashcards.config.security;

import java.util.UUID;

public class QueryContextImpl implements QueryContext {

    public QueryContextImpl() {

    }

    @Override
    public UUID getAndClearUserId() {
        return null;
    }

}
