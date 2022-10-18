package com.bakuard.flashcards.config.security;

import com.bakuard.flashcards.model.auth.credential.Credential;
import com.bakuard.flashcards.service.JwsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.UUID;

public class JwsAuthenticationProvider implements AuthenticationProvider {

    private final JwsService jwsService;

    @Autowired
    public JwsAuthenticationProvider(JwsService jwsService) {
        this.jwsService = jwsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwsAuthentication request = (JwsAuthentication) authentication;

        String jws = request.getJws();

        Object jwsBody = jwsService.parseJws(jws, bodyTypeName -> {
            Class<?> bodyType = null;
            if(bodyTypeName.equals(UUID.class.getName())) bodyType = UUID.class;
            else if(bodyTypeName.equals(Credential.class.getName())) bodyType = Credential.class;
            return bodyType;
        }).orElseThrow();

        JwsAuthentication response = new JwsAuthentication(jws, jwsBody);
        response.setAuthenticated(true);
        return response;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwsAuthentication.class.isAssignableFrom(authentication);
    }

}
