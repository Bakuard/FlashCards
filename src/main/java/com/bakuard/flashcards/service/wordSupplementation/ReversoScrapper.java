package com.bakuard.flashcards.service.wordSupplementation;

import com.bakuard.flashcards.model.word.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
        List<WordTranslation> translations = word.getTranslationsBy(outerSourceName);
        if(translations.isEmpty() ||
                durationGreaterThan(translationsRecentUpdateDate(translations), 90)) {
            try {
                translateWord(word.getValue()).forEach(word::mergeTranslation);
            } catch (Exception e) {
                logger.warn("Fail to get translates for word '{}' from Reverso. Reason: {}", word.getValue(), e);
            }
        }

        List<WordExample> examples = word.getExamplesBy(outerSourceName);
        if(!examples.isEmpty() && (
                examples.stream().anyMatch(example -> example.getTranslate() == null) ||
                durationGreaterThan(examplesRecentUpdateDate(examples), 90)
        )) {
            try {

            } catch (Exception e) {
                logger.warn("Fail to translate examples for word '{}' from Reverso. Reason: {}", word.getValue(), e);
            }
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
                                new OuterSource(toUrlForReversoUi(word),
                                        outerSourceName,
                                        LocalDate.now(clock))
                        )).
                toList();
    }

    private String translateExample(String example) throws Exception {
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
                        """.formatted(example)
                )).
                timeout(Duration.ofSeconds(10)).
                build();

        HttpResponse<String> response = HttpClient.newBuilder().
                build().
                send(request, HttpResponse.BodyHandlers.ofString());

        Iterator<JsonNode> iterator = mapper.readTree(response.body()).
                findPath("translation").
                iterator();
        String result = null;
        if (iterator.hasNext()) result = iterator.next().textValue();
        return result;
    }

    private boolean durationGreaterThan(LocalDate recentUpdateDate, int days) {
        return Duration.between(
                        recentUpdateDate,
                        LocalDate.now(clock)).
                abs().
                get(ChronoUnit.DAYS) > days;
    }

    private LocalDate translationsRecentUpdateDate(List<WordTranslation> translations) {
        return translations.stream().
                flatMap(translation -> translation.getSourceInfo().stream()).
                findFirst().
                map(OuterSource::recentUpdateDate).
                orElseThrow();
    }

    private LocalDate examplesRecentUpdateDate(List<WordExample> examples) {
        return examples.stream().
                flatMap(example -> example.getSourceInfo().stream()).
                findFirst().
                map(ExampleOuterSource::recentUpdateDate).
                orElseThrow();
    }

    private String toUrlForReversoUi(String word) {
        return "https://context.reverso.net/translation/english-russian/" + word;
    }

}
