package com.bakuard.flashcards.service;

import com.bakuard.flashcards.config.SpringConfig;
import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.config.configData.ConfigData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:test.properties")
@Import({SpringConfig.class, TestConfig.class})
class JwsServiceTest {

    @Autowired
    private ConfigData conf;
    @Autowired
    private ObjectMapper mapper;
    private Clock clock = Clock.fixed(Instant.parse("2100-01-01T00:00:00Z"), ZoneId.of("Asia/Kolkata"));

    @Test
    @DisplayName("""
            generateJws(jwsBody, keyName):
             jwsBody is null
             => throw exception
            """)
    public void generateJws1() {
        JwsService jwsService = new JwsService(conf, clock, mapper);

        Assertions.assertThatNullPointerException().
                isThrownBy(() -> jwsService.generateJws(null, "key", conf.jws().commonTokenLifeTime()));
    }

    @Test
    @DisplayName("""
            generateJws(jwsBody, keyName):
             keyName is null
             => throw exception
            """)
    public void generateJws2() {
        JwsService jwsService = new JwsService(conf, clock, mapper);

        Assertions.assertThatNullPointerException().
                isThrownBy(() -> jwsService.generateJws(new Object(), null, conf.jws().commonTokenLifeTime()));
    }

    @Test
    @DisplayName("""
            parseJws(jws):
             jws is null
             => throw exception
            """)
    public void parseJws1() {
        JwsService jwsService = new JwsService(conf, clock, mapper);

        Assertions.assertThatNullPointerException().
                isThrownBy(() -> jwsService.parseJws(null, "any key"));
    }

    @Test
    @DisplayName("""
            generate and parse token:
             JWS body is correct
             => parser must return the same JWS body
            """)
    public void generateAndParse1() {
        UUID expected = toUUID(1);
        JwsService jwsService = new JwsService(conf, clock, mapper);

        String jws = jwsService.generateJws(expected, "key", conf.jws().commonTokenLifeTime());
        UUID actual = jwsService.parseJws(jws, "key");

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            generate and parse token:
             generate several JWS for different jws bodies,
             use unique key-pair for each JWS
             => parser must return correct JWS body for each JWS
            """)
    public void generateAndParse2() {
        JwsService jwsService = new JwsService(conf, clock, mapper);
        String jws1 = jwsService.generateJws(toUUID(1), "key1", conf.jws().commonTokenLifeTime());
        String jws2 = jwsService.generateJws(toUUID(2), "key2", conf.jws().commonTokenLifeTime());

        UUID actual1 = jwsService.parseJws(jws1, "key1");
        UUID actual2 = jwsService.parseJws(jws2, "key2");

        Assertions.assertThat(actual1).isEqualTo(toUUID(1));
        Assertions.assertThat(actual2).isEqualTo(toUUID(2));
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

}