package com.bakuard.flashcards.service;

import com.bakuard.flashcards.config.configData.ConfigData;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.KeyPair;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class JwsService {

    private ConfigData configData;
    private Clock clock;
    private Map<String, KeyPair> keyPairs;
    private ObjectMapper objectMapper;

    public JwsService(ConfigData configData, Clock clock) {
        this.configData = configData;
        this.clock = clock;
        this.keyPairs = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();
    }


    public String generateJws(Object jwsBody, String keyName) {
        LocalDateTime expiration = LocalDateTime.now(clock).plusDays(configData.jwsLifeTimeInDays());
        String json = tryCatch(() -> objectMapper.writeValueAsString(jwsBody));
        KeyPair keyPair = keyPairs.computeIfAbsent(keyName, key -> Keys.keyPairFor(SignatureAlgorithm.RS512));

        return Jwts.builder().
                setExpiration(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant())).
                claim("body", json).
                claim("bodyType", jwsBody.getClass().getName()).
                claim("keyName", keyName).
                signWith(keyPair.getPrivate()).
                compact();
    }

    public <T> T parseJws(String jws, Class<T> jwsBodyType) {
        KeyPair keyPair = keyPairs.get(parseKeyPairName(jws));
        Claims claims = parseJws(jws, keyPair);
        String json = claims.get("body", String.class);
        return tryCatch(() -> objectMapper.readValue(json, jwsBodyType));
    }

    public <T> Optional<T> parseJws(String jws, Function<String, Class<T>> jwsBodyTypeMapper) {
        KeyPair keyPair = keyPairs.get(parseKeyPairName(jws));
        Claims claims = parseJws(jws, keyPair);
        String json = claims.get("body", String.class);
        Class<T> bodyType = jwsBodyTypeMapper.apply(claims.get("bodyType", String.class));
        T body = bodyType == null ? null : tryCatch(() -> objectMapper.readValue(json, bodyType));
        return Optional.ofNullable(body);
    }


    private Claims parseJws(String jws, KeyPair keyPair) {
        if(jws.startsWith("Bearer ")) jws = jws.substring(7);

        return Jwts.parserBuilder().
                setSigningKey(keyPair.getPublic()).
                build().
                parseClaimsJws(jws).
                getBody();
    }

    private String parseKeyPairName(String jws) {
        final String preparedJws = jws.startsWith("Bearer ") ? jws.substring(7) : jws;

        return tryCatch(() -> objectMapper.readTree(decodeJwsBody(preparedJws))).
                findPath("keyName").
                textValue();
    }

    private String decodeJwsBody(String jws) {
        String[] data = jws.split("\\.");
        return new String(Base64.getUrlDecoder().decode(data[1]));
    }

    private <T> T tryCatch(Callable<T> callable) {
        try {
            return callable.call();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

}
