package com.bakuard.flashcards.config.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class RequestContextImpl implements RequestContext {

    public RequestContextImpl() {

    }

    @Override
    public <T> T getCurrentJwsBodyAs(Class<T> jwsBodyType) {
        JwsAuthentication jwsAuthentication = (JwsAuthentication) SecurityContextHolder.
                getContext().
                getAuthentication();
        return jwsBodyType.cast(jwsAuthentication.getJwsBody());
    }

}
