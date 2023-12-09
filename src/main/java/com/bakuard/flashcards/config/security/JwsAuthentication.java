package com.bakuard.flashcards.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwsAuthentication implements Authentication {

    private final String jws;
    private Object jwsBody;
    private String path;

    public JwsAuthentication(String jws, Object jwsBody, String path) {
        this.jws = jws;
        this.jwsBody = jwsBody;
        this.path = path;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {}

    @Override
    public String getName() {
        return null;
    }

    public String getJws() {
        return jws;
    }

    public Object getJwsBody() {
        return jwsBody;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "JwsAuthentication{" +
                "jws='" + jws + '\'' +
                ", jwsBody=" + jwsBody +
                ", path='" + path + '\'' +
                '}';
    }

}
