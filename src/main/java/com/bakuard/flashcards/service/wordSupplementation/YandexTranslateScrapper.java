package com.bakuard.flashcards.service.wordSupplementation;

import com.bakuard.flashcards.dal.WordOuterSourceBuffer;
import com.bakuard.flashcards.model.word.*;
import com.bakuard.flashcards.model.word.supplementation.SupplementedWord;
import com.bakuard.flashcards.model.word.supplementation.SupplementedWordExample;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Отвечает за дополнение слова переводами и переводами примеров из <a href="https://translate.yandex.ru/?utm_source=yamain&utm_medium=personal">Yandex translate</a>
 */
public class YandexTranslateScrapper implements WordSupplementation {

    private static final Logger logger = LoggerFactory.getLogger(YandexTranslateScrapper.class.getName());


    private final ObjectMapper mapper;
    private final Clock clock;
    private final String outerSourceName = "Yandex";
    private final WordOuterSourceBuffer wordOuterSourceBuffer;
    private final TransactionTemplate transaction;

    public YandexTranslateScrapper(ObjectMapper mapper,
                                   Clock clock,
                                   WordOuterSourceBuffer wordOuterSourceBuffer,
                                   TransactionTemplate transaction) {
        this.mapper = mapper;
        this.clock = clock;
        this.wordOuterSourceBuffer = wordOuterSourceBuffer;
        this.transaction = transaction;
    }

    /**
     * см. {@link WordSupplementation#supplement(Word)}
     */
    @Override
    public SupplementedWord supplement(Word word) {
        SupplementedWord result = transaction.execute(status ->
                wordOuterSourceBuffer.findByWordValueAndOuterSource(
                                outerSourceName, word.getValue(), word.getUserId()).
                        orElseGet(() -> new SupplementedWord(
                                word.getUserId(),
                                word.getValue(),
                                outerSourceName,
                                LocalDate.now(clock),
                                toUri(toUrlForYandexUi(word.getValue()))
                        ))
        );

        if(result.getTranslations().isEmpty() || result.getDaysAfterRecentUpdateDate(clock) > 90) {
            try {
                logger.info("get translations for word '{}' from {}", word, outerSourceName);

                result.addTranslations(translateWord(word.getValue()));
            } catch (Exception e) {
                logger.warn("Fail to get translations for word '{}' from {}. Reason: {}",
                        word, outerSourceName, e);
            }
        }

        result.removeRedundantExamples(word.getExamples());
        try {
            if(result.getDaysAfterRecentUpdateDate(clock) > 90) {
                for(SupplementedWordExample example : result.getExamples()) {
                    logger.info("replace example '{}' for word '{}' from {} of user {}",
                            example.getOrigin(), word.getValue(), outerSourceName, word.getUserId());
                    result.replaceExample(
                            example.getOrigin(),
                            translateExample(example.getOrigin())
                    );
                }
            }

            for(WordExample example : result.getMissingExamples(word.getExamples())) {
                logger.info("add example '{}' for word '{}' from {} of user {}",
                        example.getOrigin(), word.getValue(), outerSourceName, word.getUserId());
                result.addExample(translateExample(example.getOrigin()));
            }
        } catch(Exception e) {
            logger.warn("Fail to get examples for word '{}' from {}. Reason: {}",
                    word, outerSourceName, e);
        }

        return result;
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
                result.add(new WordTranslation(translateValue, null));

                Iterator<JsonNode> synonyms = translate.findPath("syn").iterator();
                while(synonyms.hasNext()) {
                    JsonNode synonym = synonyms.next();
                    translateValue = synonym.findPath("text").textValue();
                    result.add(new WordTranslation(translateValue, null));
                }
            }
        }
        return result;
    }

    private SupplementedWordExample translateExample(String example) throws Exception {
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
                header("referer", toUrlForYandexUi(example)).
                header("accept", "*/*").
                header("accept-language", "ru,en;q=0.9,en-GB;q=0.8,en-US;q=0.7").
                header("content-type", "application/x-www-form-urlencoded").
                header("x-retpath-y", "https://translate.yandex.ru").
                POST(HttpRequest.BodyPublishers.ofString(
                        "text=" + URLEncoder.encode(example, StandardCharsets.UTF_8) + "&options=4"
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
            return new SupplementedWordExample(
                    example,
                    exampleTranslate,
                    null,
                    toUri(toUrlForYandexUi(example))
            );
        } else {
            throw new IllegalStateException("Fail to load example '" + example +
                    "'. Raw body is -> " + rawResponse.body());
        }
    }

    private String toUrlForYandexUi(String text) {
        return "https://translate.yandex.ru/?utm_source=yamain" +
                "&utm_medium=personal" +
                "&source_lang=en" +
                "&target_lang=ru" +
                "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    private URI toUri(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
