package com.bakuard.flashcards.config.security;

public class RequestContextImpl implements RequestContext {

    public RequestContextImpl() {

    }

    @Override
    public <T> T getCurrentJwsBody(Class<T> jwsBodyType) {
        return null;
    }

}
