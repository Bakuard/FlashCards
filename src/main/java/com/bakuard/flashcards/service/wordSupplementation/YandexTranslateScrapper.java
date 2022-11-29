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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class YandexTranslateScrapper implements WordSupplementation {

    private static Logger logger = LoggerFactory.getLogger(YandexTranslateScrapper.class.getName());


    private final ObjectMapper mapper;
    private final Clock clock;
    private final String outerSourceName = "Yandex";

    public YandexTranslateScrapper(ObjectMapper mapper,
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
                uri(new URI("https://dictionary.yandex.net/dicservice.json/lookupMultiple?" +
                        "sid=ff07bd7d.63677c92.0e66e0a9.74722d74657874&" +
                        "ui=ru&" +
                        "srv=tr-text&" +
                        "text=" + URLEncoder.encode(word, StandardCharsets.UTF_8) + "&" +
                        "type=regular%2Csyn%2Cant%2Cderiv&" +
                        "lang=en-ru&" +
                        "flags=15783&" +
                        "dict=en-ru.regular%2Cen.syn%2Cen.ant%2Cen.deriv&" +
                        "yu=2603617871649255206&" +
                        "yum=1651036922745260963")).
                header("User-Agent", RandomUserAgent.getRandomUserAgent()).
                header("authority", "dictionary.yandex.net").
                header("origin", "https://translate.yandex.ru").
                GET().
                timeout(Duration.ofSeconds(5)).
                build();

        HttpResponse<String> rawResponse = HttpClient.newBuilder().
                build().
                send(request, HttpResponse.BodyHandlers.ofString());

        Iterator<JsonNode> regularIterator = mapper.readTree(rawResponse.body()).
                findPath("en-ru").
                findPath("regular").
                iterator();

        ArrayList<WordTranslation> result = new ArrayList<>();
        while(regularIterator.hasNext()) {
            Iterator<JsonNode> trIterator = regularIterator.next().findPath("tr").iterator();
            while(trIterator.hasNext()) {
                JsonNode translate = trIterator.next();
                String translateValue = translate.findPath("text").textValue();
                result.add(
                        new WordTranslation(translateValue, null).
                                addSourceInfo(
                                        new OuterSource(toUrlForYandexUi(word),
                                                outerSourceName,
                                                LocalDate.now(clock))
                                )
                );

                Iterator<JsonNode> synonyms = translate.findPath("syn").iterator();
                while(synonyms.hasNext()) {
                    JsonNode synonym = synonyms.next();
                    translateValue = synonym.findPath("text").textValue();
                    result.add(
                            new WordTranslation(translateValue, null).
                                    addSourceInfo(
                                            new OuterSource(toUrlForYandexUi(word),
                                                    outerSourceName,
                                                    LocalDate.now(clock))
                                    )
                    );
                }
            }
        }
        return result;
    }

    private void translateExample(WordExample example) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().
                uri(new URI("https://translate.yandex.net/api/v1/tr.json/translate?" +
                        "id=" + UUID.randomUUID() + "-0-0" +
                        "srv=tr-text&" +
                        "source_lang=en&" +
                        "target_lang=ru&" +
                        "reason=paste&" +
                        "format=text&" +
                        "ajax=1&" +
                        "yu=2603617871649255206&" +
                        "yum=1651036922745260963")).
                header("User-Agent", RandomUserAgent.getRandomUserAgent()).
                header("authority", "translate.yandex.net").
                header("origin", "https://translate.yandex.ru").
                header("referer", toUrlForYandexUi(example.getOrigin())).
                header("accept", "*/*").
                header("accept-language", "ru,en;q=0.9,en-GB;q=0.8,en-US;q=0.7").
                header("content-type", "application/x-www-form-urlencoded").
                header("x-retpath-y", "https://translate.yandex.ru").
                POST(HttpRequest.BodyPublishers.ofString(
                        "text=" + URLEncoder.encode(example.getOrigin(), StandardCharsets.UTF_8) + "&options=4"
                )).
                timeout(Duration.ofSeconds(5)).
                build();

        HttpResponse<String> rawResponse = HttpClient.newBuilder().
                build().
                send(request, HttpResponse.BodyHandlers.ofString());

        Iterator<JsonNode> iterator = mapper.readTree(rawResponse.body()).
                findPath("text").
                iterator();
        if(iterator.hasNext()) {
            String exampleTranslate = iterator.next().textValue();
            if(example.getTranslate() == null) example.setTranslate(exampleTranslate);
            example.addSourceInfo(
                    new ExampleOuterSource(
                            toUrlForYandexUi(example.getOrigin()),
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

    private String toUrlForYandexUi(String text) {
        return "https://translate.yandex.ru/?utm_source=yamain" +
                "&utm_medium=personal" +
                "&source_lang=en" +
                "&target_lang=ru" +
                "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

}
