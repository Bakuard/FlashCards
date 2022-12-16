package com.bakuard.flashcards.service;

import com.bakuard.flashcards.config.configData.ConfigData;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
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

    /**
     * Создает новый сервис генерации и парсинг JWS токенов.
     * @param configData общие данные конфигурации приложения
     * @param clock часы используемые для получения текущей даты (параметр добавлен для удобства тестирования)
     */
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
        String json = tryCatch(() -> objectMapper.writeValueAsString(jwsBody), RuntimeException::new);
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
     * @throws JwtException если выполняется хотя бы одна из следующих причин: <br/>
     *                      1. если указанный токен не соответствует формату JWT. <br/>
     *                      2. если срок действия токена истек. <br/>
     *                      3. если токен был изменен после его подписания. <br/>
     */
    public <T> T parseJws(String jws) {
        Objects.requireNonNull(jws, "jws can't be null");

        String keyPairName = parseKeyPairName(jws);
        KeyPair keyPair = getKeyPairByName(keyPairName);

        Claims claims = parseJws(jws, keyPair);
        String json = claims.get("body", String.class);
        Class<?> jwsBodyType = tryCatch(() -> Class.forName(claims.get("bodyType", String.class)), RuntimeException::new);
        return tryCatch(() -> objectMapper.readValue(json, (Class<T>) jwsBodyType), RuntimeException::new);
    }


    private Claims parseJws(String jws, KeyPair keyPair) {
        if(jws.startsWith("Bearer ")) jws = jws.substring(7);

        return Jwts.parserBuilder().
                setSigningKey(keyPair.getPublic()).
                build().
                parseClaimsJws(jws).
                getBody();
    }

    private KeyPair getKeyPairByName(String keyPairName) {
        KeyPair keyPair = keyPairs.get(keyPairName);
        if(keyPair == null) throw new MalformedJwtException("Unknown key-pair with name '" + keyPairName + '\'');
        return keyPair;
    }

    private String parseKeyPairName(String jws) {
        final String preparedJws = jws.startsWith("Bearer ") ? jws.substring(7) : jws;

        String keyPairName =  tryCatch(() -> objectMapper.readTree(decodeJwsBody(preparedJws)),
                e -> new MalformedJwtException("Jws has incorrect format '" + jws + '\'')).
                findPath("keyName").
                textValue();
        if(keyPairName == null) throw new MalformedJwtException("Missing key name");
        return keyPairName;
    }

    private String decodeJwsBody(String jws) {
        String[] data = jws.split("\\.");
        return new String(Base64.getUrlDecoder().decode(data[1]));
    }

    private <T> T tryCatch(Callable<T> callable,
                           Function<Exception, ? extends RuntimeException> exceptionFabric) {
        try {
            return callable.call();
        } catch(Exception e) {
            throw exceptionFabric.apply(e);
        }
    }

}
