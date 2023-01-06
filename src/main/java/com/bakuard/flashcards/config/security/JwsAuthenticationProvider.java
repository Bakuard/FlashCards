package com.bakuard.flashcards.config.security;

import com.bakuard.flashcards.service.JwsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class JwsAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwsAuthenticationProvider.class.getName());


    private final JwsService jwsService;

    @Autowired
    public JwsAuthenticationProvider(JwsService jwsService) {
        this.jwsService = jwsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwsAuthentication request = (JwsAuthentication) authentication;

        String jws = request.getJws();

        try {
            Object jwsBody = null;
            switch(request.getPath()) {
                case "/users/registration/finalStep" -> jwsBody = jwsService.parseJws(jws, "register");
                case "/users/restorePassword/finalStep" -> jwsBody = jwsService.parseJws(jws, "restorePassword");
                case "/users/deletion/finalStep" -> jwsBody = jwsService.parseJws(jws, "delete");
                default -> jwsBody = jwsService.parseJws(jws, "common");
            }

            JwsAuthentication response = new JwsAuthentication(jws, jwsBody, request.getPath());
            response.setAuthenticated(true);
            return response;
        } catch(Exception e) {
            logger.error("Fail authentication for jws = {}", request, e);
            throw new BadCredentialsException("Incorrect JWS -> " + jws, e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwsAuthentication.class.isAssignableFrom(authentication);
    }

}
