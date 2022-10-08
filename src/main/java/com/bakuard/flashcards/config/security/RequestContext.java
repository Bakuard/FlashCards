package com.bakuard.flashcards.config.security;

public interface RequestContext {

    public <T> T getCurrentJwsBody(Class<T> jwsBodyType);

}
