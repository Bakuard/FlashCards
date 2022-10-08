package com.bakuard.flashcards.service;

import com.bakuard.flashcards.config.ConfigData;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.KeyPair;
import java.security.PublicKey;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class JwsService {

    private ConfigData configData;
    private Clock clock;
    private KeyPair keyPair;
    private ObjectMapper objectMapper;

    public JwsService(ConfigData configData, Clock clock) {
        this.configData = configData;
        this.clock = clock;
        this.keyPair = Keys.keyPairFor(SignatureAlgorithm.RS512);
        this.objectMapper = new ObjectMapper();
    }


    public String generateJws(Object jwsBody) {
        LocalDateTime expiration = LocalDateTime.now(clock).plusDays(configData.jwsLifeTimeInDays());
        String json = tryCatch(() -> objectMapper.writeValueAsString(jwsBody));

        return Jwts.builder().
                setExpiration(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant())).
                claim("body", json).
                claim("bodyType", jwsBody.getClass().getName()).
                signWith(keyPair.getPrivate()).
                compact();
    }

    public <T> T parseJws(String jws, Class<T> jwsBodyType) {
        Claims claims = parseJws(jws, keyPair);
        String json = claims.get("body", String.class);
        return tryCatch(() -> objectMapper.readValue(json, jwsBodyType));
    }

    public <T> Optional<T> parseJws(String jws, Function<String, Class<T>> jwsBodyTypeMapper) {
        Claims claims = parseJws(jws, keyPair);
        String json = claims.get("body", String.class);
        Class<T> bodyType = jwsBodyTypeMapper.apply(claims.get("bodyType", String.class));
        T body = bodyType == null ? null : tryCatch(() -> objectMapper.readValue(json, bodyType));
        return Optional.ofNullable(body);
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public String decodeJws(String jws) {
        String[] data = jws.split("\\.");
        return Arrays.stream(data).
                limit(2).
                map(part -> Base64.getUrlDecoder().decode(part)).
                map(String::new).
                reduce((a, b) -> String.join(".", a, b)).
                map(result -> String.join(".", result, data[2])).
                orElse("empty jws");
    }


    private Claims parseJws(String jws, KeyPair keyPair) {
        if(jws.startsWith("Bearer ")) jws = jws.substring(7);

        return Jwts.parserBuilder().
                setSigningKey(keyPair.getPublic()).
                build().
                parseClaimsJws(jws).
                getBody();
    }

    private <T> T tryCatch(Callable<T> callable) {
        try {
            return callable.call();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

}
