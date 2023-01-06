package com.bakuard.flashcards.service.wordSupplementation;

import com.bakuard.flashcards.dal.WordOuterSourceBuffer;
import com.bakuard.flashcards.model.word.supplementation.SupplementedWord;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.model.word.WordInterpretation;
import com.bakuard.flashcards.model.word.WordTranscription;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Отвечает за дополнение слова транскрипциями и толкованиями из <a href="https://www.oxfordlearnersdictionaries.com/">Oxford Dictionary</a>
 */
public class OxfordDictionaryScrapper implements WordSupplementation {

    private static final Logger logger = LoggerFactory.getLogger(OxfordDictionaryScrapper.class.getName());


    private final Clock clock;
    private final String outerSourceName = "OxfordDictionary";
    private final WordOuterSourceBuffer wordOuterSourceBuffer;
    private final TransactionTemplate transaction;

    public OxfordDictionaryScrapper(Clock clock,
                                    WordOuterSourceBuffer wordOuterSourceBuffer,
                                    TransactionTemplate transaction) {
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
                                toUri(toUrlOxfordDictionaryUi(word.getValue()))
                        ))
        );

        if(result.getTranscriptions().isEmpty() || result.getMonthsAfterRecentUpdateDate(clock) > 12) {
            try {
                logger.info("get transcriptions for word '{}' from {}", word, outerSourceName);

                result.addTranscriptions(transcriptions(word.getValue()));
            } catch (Exception e) {
                logger.warn("Fail to get transcriptions for word '{}' from {}. Reason: {}",
                        word, outerSourceName, e);
            }
        }

        if(result.getInterpretations().isEmpty() || result.getDaysAfterRecentUpdateDate(clock) > 90) {
            try {
                logger.info("get interpretations for word '{}' from {}", word, outerSourceName);

                result.addInterpretations(interpretations(word.getValue()));
            } catch (Exception e) {
                logger.warn("Fail to get transcriptions for word '{}' from {}. Reason: {}",
                        word, outerSourceName, e);
            }
        }

        return result;
    }


    private List<WordTranscription> transcriptions(String word) throws Exception {
        Document document = Jsoup.parse(loadRawBody(toUrlOxfordDictionaryUi(word)));
        Elements elements = document.select(".phon");
        return elements.stream().
                map(Element::text).
                map(transcription -> transcription.replace("/", "")).
                map(transcription -> new WordTranscription(transcription, null)).
                toList();
    }

    private List<WordInterpretation> interpretations(String word) throws Exception {
        String rawBody = loadRawBody(toUrlOxfordDictionaryUi(word));

        Document document = Jsoup.parse(rawBody);
        Elements elements = document.select("div.responsive_row#relatedentries ul.list-col a[href^=" + toUrlOxfordDictionaryTemplate(word) + "_]");
        Set<String> otherPartOfSpeechUrls = Set.copyOf(elements.eachAttr("href"));

        List<String> rawBodies = new ArrayList<>();
        rawBodies.add(rawBody);
        for(String url : otherPartOfSpeechUrls) rawBodies.add(loadRawBody(url));
        return rawBodies.stream().
                flatMap(raw -> parseInterpretations(raw, word).stream()).
                toList();
    }

    private List<WordInterpretation> parseInterpretations(String rawBody, String word) {
        Document document = Jsoup.parse(rawBody);
        Elements elements = document.select("div.entry>ol li.sense");
        return elements.stream().
                map(element -> element.select("span.grammar").text() +
                        element.select("span.def").text()).
                map(WordInterpretation::new).
                collect(Collectors.toCollection(ArrayList::new));
    }

    private String loadRawBody(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().
                uri(new URI(url)).
                header("User-Agent", RandomUserAgent.getRandomUserAgent()).
                header("Content-Type", "text/plain").
                GET().
                timeout(Duration.ofSeconds(10)).
                build();

        HttpResponse<String> rawResponse = HttpClient.newBuilder().
                build().
                send(request, HttpResponse.BodyHandlers.ofString());

        if(rawResponse.statusCode() == 302) {
            return loadRawBody(rawResponse.headers().firstValue("location").orElseThrow());
        }
        return rawResponse.body();
    }

    private String toUrlOxfordDictionaryUi(String word) {
        return toUrlOxfordDictionaryTemplate(word) + "_1";
    }

    private String toUrlOxfordDictionaryTemplate(String word) {
        return "https://www.oxfordlearnersdictionaries.com/definition/english/" + word;
    }

    private URI toUri(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
