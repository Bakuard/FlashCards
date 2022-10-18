package com.bakuard.flashcards.config.security;

public interface RequestContext {

    public <T> T getCurrentJwsBodyAs(Class<T> jwsBodyType);

}
