package com.bakuard.flashcards.config.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwsAuthentication extends AbstractAuthenticationToken {

    private final String jws;
    private Object jwsBody;
    private String path;

    public JwsAuthentication(String jws, String path) {
        super(null);
        this.jws = jws;
        this.path = path;
    }

    public JwsAuthentication(String jws, Object jwsBody, String path) {
        super(null);
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
