package com.bakuard.flashcards.config;

import com.bakuard.flashcards.config.security.JwsFilter;
import com.bakuard.flashcards.controller.message.Messages;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.service.JwsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class.getName());

    private JwsService jwsService;
    private DtoMapper mapper;
    private ObjectMapper jsonWriter;
    private Messages messages;

    @Autowired
    public SecurityConfig(JwsService jwsService, DtoMapper mapper, Messages messages) {
        this.jwsService = jwsService;
        this.mapper = mapper;
        this.messages = messages;
        jsonWriter = new ObjectMapper();
        jsonWriter.registerModule(new JavaTimeModule());
        jsonWriter.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable).
                cors(Customizer.withDefaults()).
                sessionManagement(
                        sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ).
                exceptionHandling(
                        exceptionConfig -> exceptionConfig.authenticationEntryPoint((request, response, ex) -> {
                            logger.error("Security fail", ex);

                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                            jsonWriter.writeValue(
                                    response.getOutputStream(),
                                    mapper.toExceptionResponse(HttpStatus.UNAUTHORIZED,
                                            messages.getMessage("unauthorized"))
                            );
                        })
                ).
                authorizeHttpRequests(
                        authRequestConf -> authRequestConf.requestMatchers(
                                "/api",
                                        "/apiStandardFormat/**",
                                        "/swagger-ui/**",
                                        "/users/registration/firstStep",
                                        "/users/restorePassword/firstStep",
                                        "/users/enter"
                                ).permitAll().
                                anyRequest().authenticated()
                ).
                addFilterBefore(new JwsFilter(jwsService), UsernamePasswordAuthenticationFilter.class).
                build();
    }
}
