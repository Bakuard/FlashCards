package com.bakuard.flashcards.config.security;

import java.util.UUID;

public class RequestContextImpl implements RequestContext {

    public RequestContextImpl() {

    }

    @Override
    public UUID getCurrentJwsBody() {
        return null;
    }

}
