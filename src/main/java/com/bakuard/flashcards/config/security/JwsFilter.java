package com.bakuard.flashcards.config.security;

import com.bakuard.flashcards.service.JwsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwsFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwsFilter.class.getName());


    private final JwsService jwsService;

    public JwsFilter(JwsService jwsService) {
        this.jwsService = jwsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(token != null && token.startsWith("Bearer ") &&
                SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Object jwsBody = null;
                switch(request.getRequestURI()) {
                    case "/users/registration/finalStep" -> jwsBody = jwsService.parseJws(token, "register");
                    case "/users/restorePassword/finalStep" -> jwsBody = jwsService.parseJws(token, "restorePassword");
                    case "/users/deletion/finalStep" -> jwsBody = jwsService.parseJws(token, "delete");
                    default -> jwsBody = jwsService.parseJws(token, "common");
                }

                SecurityContextHolder.getContext().setAuthentication(
                        new JwsAuthentication(token, jwsBody, request.getRequestURI())
                );
            } catch(Exception e) {
                logger.error("Fail authentication:path={}, jws={}", request.getRequestURI(), request, e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
