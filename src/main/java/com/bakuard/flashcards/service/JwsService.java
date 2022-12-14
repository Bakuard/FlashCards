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
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Отвечает за генерацию и парсинг JWS токенов.
 */
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

    /**
     * Генерирует и возвращает JWS токен для которого в качестве body будет взят объект jwsBody
     * сериализованный в JSON формат. Токен подписывается с применением ассиметричного шифрования, где
     * используется пара ключей с именем keyName. Если пары ключей с таким именем нет - она будет автоматически
     * сгенерирована.
     * @param jwsBody тело JWS токена
     * @param keyName имя пары ключей используемых для подписи токена
     * @return JWS токен
     * @throws NullPointerException если jwsBody или keyName имеют значение null.
     */
    public String generateJws(Object jwsBody, String keyName) {
        Objects.requireNonNull(jwsBody, "jwsBody can't be null");
        Objects.requireNonNull(keyName, "keyName can't be null");

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

    /**
     * Парсит переданный JWS токен и десериализует его тело в виде отдельного объекта с типом T.
     * @param jws токен
     * @param <T> тип объекта представляющего десериализованное тело токена
     * @return тело токена в виде отдельного объекта с типом T.
     * @throws NullPointerException если jws равен null.
     */
    public <T> T parseJws(String jws) {
        Objects.requireNonNull(jws, "jws can't be null");

        String keyPairName = parseKeyPairName(jws);
        KeyPair keyPair = keyPairs.get(keyPairName);
        if(keyPair == null) {
            throw new IllegalStateException("Unknown key-pair with name '" + keyPairName + '\'');
        }

        Claims claims = parseJws(jws, keyPair);
        String json = claims.get("body", String.class);
        Class<?> jwsBodyType = tryCatch(() -> Class.forName(claims.get("bodyType", String.class)));
        return tryCatch(() -> objectMapper.readValue(json, (Class<T>) jwsBodyType));
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
