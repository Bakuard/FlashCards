package com.bakuard.flashcards.service;

import com.bakuard.flashcards.config.ConfigData;
import com.bakuard.flashcards.config.TestConfig;
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
import java.util.Optional;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:test.properties")
@Import(TestConfig.class)
class JwsServiceTest {

    @Autowired
    private ConfigData configData;
    private Clock clock = Clock.fixed(Instant.parse("2100-01-01T00:00:00Z"), ZoneId.of("Asia/Kolkata"));

    @Test
    @DisplayName("""
            generate and parse token:
             Jws body is correct
             => parser must return the same Jws body
            """)
    public void generateAndParse1() {
        UUID expected = toUUID(1);
        JwsService jwsService = new JwsService(configData, clock);

        String jws = jwsService.generateJws(expected, "keyName");
        UUID actual = jwsService.parseJws(jws, UUID.class);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            generate and parse token:
             Jws body is correct,
             use parser with type mapper
             => parser must return the same Jws body
            """)
    public void generateAndParse2() {
        UUID expected = toUUID(1);
        JwsService jwsService = new JwsService(configData, clock);

        String jws = jwsService.generateJws(expected, "keyName");
        Optional<UUID> actual = jwsService.parseJws(jws,
                typeName -> typeName.equals(UUID.class.getName()) ? UUID.class : null);

        Assertions.assertThat(actual).
                isPresent().
                contains(expected);
    }

    @Test
    @DisplayName("""
            generate and parse token:
             Jws body is correct,
             use parser with type mapper,
             type mapper return null
             => parser must return the same Jws body
            """)
    public void generateAndParse3() {
        UUID expected = toUUID(1);
        JwsService jwsService = new JwsService(configData, clock);

        String jws = jwsService.generateJws(expected, "keyName");
        Optional<UUID> actual = jwsService.parseJws(jws, typeName -> null);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            generate and parse token:
             Jws body is correct,
             use several keys with different names
             => parser must return the same Jws body
            """)
    public void generateAndParse4() {
        JwsService jwsService = new JwsService(configData, clock);
        String jws1 = jwsService.generateJws(toUUID(1), "keyName1");
        String jws2 = jwsService.generateJws(toUUID(2), "keyName2");

        UUID actual1 = jwsService.parseJws(jws1, UUID.class);
        Optional<UUID> actual2 = jwsService.parseJws(jws2, typeName -> UUID.class);

        Assertions.assertThat(actual1).isEqualTo(toUUID(1));
        Assertions.assertThat(actual2).
                isPresent().
                contains(toUUID(2));
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

}