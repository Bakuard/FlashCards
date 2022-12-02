package com.bakuard.flashcards.config;

import com.bakuard.flashcards.config.security.JwsAuthenticationProvider;
import com.bakuard.flashcards.config.security.JwsFilter;
import com.bakuard.flashcards.controller.message.Messages;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.service.JwsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

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

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(new JwsAuthenticationProvider(jwsService));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().cors().and().
                sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
                and().
                exceptionHandling().authenticationEntryPoint((request, response, ex) -> {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                    jsonWriter.writeValue(
                            response.getOutputStream(),
                            mapper.toExceptionResponse(HttpStatus.UNAUTHORIZED,
                                    messages.getMessage("unauthorized"))
                    );
                }).
                and().
                authorizeRequests().
                antMatchers(
                        "/users/registration/firstStep",
                        "/users/restorePassword/firstStep",
                        "/users/enter",
                        "/api",
                        "/apiStandardFormat/**",
                        "/swagger-ui/**"
                ).permitAll().
                anyRequest().authenticated().
                and().
                addFilterBefore(new JwsFilter(), UsernamePasswordAuthenticationFilter.class);
    }

}
