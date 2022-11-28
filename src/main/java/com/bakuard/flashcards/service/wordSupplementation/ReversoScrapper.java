package com.bakuard.flashcards.service.wordSupplementation;

import com.bakuard.flashcards.model.word.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class ReversoScrapper implements WordSupplementation {

    private static Logger logger = LoggerFactory.getLogger(ReversoScrapper.class.getName());


    private final ObjectMapper mapper;
    private final Clock clock;
    private final String outerSourceName = "Reverso";

    public ReversoScrapper(ObjectMapper mapper,
                           Clock clock) {
        this.mapper = mapper;
        this.clock = clock;
    }

    @Override
    public Word supplement(Word word) {
        if(word.getTranslationsRecentUpdateDate(outerSourceName).
                map(date -> durationGreaterThan(date, 90)).
                orElse(true)) {
            try {
                logger.info("translate word '{}' from {}", word.getValue(), outerSourceName);
                translateWord(word.getValue()).forEach(word::mergeTranslation);
            } catch (Exception e) {
                logger.warn("Fail to get translates for word '{}' from {}. Reason: {}",
                        word.getValue(), outerSourceName, e);
            }
        }

        List<WordExample> examples = word.getExamples().stream().
                filter(example -> example.getRecentUpdateDate(outerSourceName).
                        map(date -> durationGreaterThan(date, 90)).
                        orElse(true)).
                toList();
        try {
            for(WordExample example : examples) {
                logger.info("translate example '{}' from {}", example.getOrigin(), outerSourceName);
                translateExample(example);
            }
        } catch (Exception e) {
            logger.warn("Fail to translate examples for word '{}' from {}. Reason: {}",
                    word.getValue(), outerSourceName, e);
        }

        return word;
    }


    private List<WordTranslation> translateWord(String word) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().
                uri(new URI("https://api.reverso.net/translate/v1/translation")).
                header("User-Agent", RandomUserAgent.getRandomUserAgent()).
                header("Content-Type", "application/json").
                header("Referer", "https://www.reverso.net/").
                POST(HttpRequest.BodyPublishers.ofString(
                        """
                        {
                           "format":"text",
                           "from":"eng",
                           "to":"rus",
                           "input":"%s",
                           "options":{
                              "sentenceSplitter":true,
                              "origin":"translation.web",
                              "contextResults":true,
                              "languageDetection":true
                           }
                        }
                        """.formatted(word)
                )).
                timeout(Duration.ofSeconds(10)).
                build();

        HttpResponse<String> response = HttpClient.newBuilder().
                build().
                send(request, HttpResponse.BodyHandlers.ofString());

        Iterator<JsonNode> iterator = mapper.readTree(response.body()).
                findPath("contextResults").
                findPath("results").
                iterator();

        Stream.Builder<JsonNode> streamBuilder = Stream.builder();
        iterator.forEachRemaining(streamBuilder);
        return streamBuilder.build().
                map(node -> node.findPath("translation").textValue()).
                map(translationValue -> new WordTranslation(translationValue, null).
                        addSourceInfo(
                                new OuterSource(
                                        toUrlForReversoUi(word),
                                        outerSourceName,
                                        LocalDate.now(clock)
                                )
                        )).
                toList();
    }

    private void translateExample(WordExample example) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().
                uri(new URI("https://api.reverso.net/translate/v1/translation")).
                header("User-Agent", RandomUserAgent.getRandomUserAgent()).
                header("Content-Type", "application/json").
                header("Referer", "https://www.reverso.net/").
                POST(HttpRequest.BodyPublishers.ofString(
                        """
                        {
                           "format":"text",
                           "from":"eng",
                           "to":"rus",
                           "input":"%s",
                           "options":{
                              "sentenceSplitter":true,
                              "origin":"translation.web",
                              "contextResults":true,
                              "languageDetection":true
                           }
                        }
                        """.formatted(example.getOrigin())
                )).
                timeout(Duration.ofSeconds(10)).
                build();

        HttpResponse<String> response = HttpClient.newBuilder().
                build().
                send(request, HttpResponse.BodyHandlers.ofString());

        Iterator<JsonNode> iterator = mapper.readTree(response.body()).
                findPath("translation").
                iterator();
        if(iterator.hasNext()) {
            String exampleTranslate = iterator.next().textValue();
            if(example.getTranslate() == null) example.setTranslate(exampleTranslate);
            example.addSourceInfo(
                    new ExampleOuterSource(
                            toUrlForReversoUi(example.getOrigin()),
                            outerSourceName,
                            LocalDate.now(clock),
                            exampleTranslate
                    )
            );
        }
    }

    private boolean durationGreaterThan(LocalDate recentUpdateDate, int days) {
        return ChronoUnit.DAYS.between(recentUpdateDate, LocalDate.now(clock)) > days;
    }

    private String toUrlForReversoUi(String text) {
        return "https://context.reverso.net/translation/english-russian/" +
                URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

}
