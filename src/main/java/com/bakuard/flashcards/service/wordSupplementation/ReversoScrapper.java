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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Отвечает за дополнение слова переводами и переводами примеров из <a href="https://www.reverso.net/text-translation">Reverso translate</a>
 */
public class ReversoScrapper implements WordSupplementation {

    private static final Logger logger = LoggerFactory.getLogger(ReversoScrapper.class.getName());


    private final ObjectMapper mapper;
    private final Clock clock;
    private final String outerSourceName = "Reverso";
    private final WordOuterSourceBuffer wordOuterSourceBuffer;
    private final TransactionTemplate transaction;

    public ReversoScrapper(ObjectMapper mapper,
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
                                toUri(toUrlForReversoUi(word.getValue()))
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

        transaction.execute(status -> {
            wordOuterSourceBuffer.save(result);
            return null;
        });

        return result;
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
                map(translationValue -> new WordTranslation(translationValue, null)).
                toList();
    }

    private SupplementedWordExample translateExample(String example) throws Exception {
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
        if(iterator.hasNext()) {
            String exampleTranslate = iterator.next().textValue();
            return new SupplementedWordExample(
                    example,
                    exampleTranslate,
                    null,
                    toUri(toUrlForReversoUi(example))
            );
        } else {
            throw new IllegalStateException("Fail to load example '" + example +
                    "'. Raw body is -> " + response.body());
        }
    }

    private String toUrlForReversoUi(String text) {
        return "https://context.reverso.net/translation/english-russian/" +
                URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    private URI toUri(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
