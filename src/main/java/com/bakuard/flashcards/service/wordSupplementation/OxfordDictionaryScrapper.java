package com.bakuard.flashcards.service.wordSupplementation;

import com.bakuard.flashcards.model.word.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OxfordDictionaryScrapper implements WordSupplementation {

    private static final Logger logger = LoggerFactory.getLogger(OxfordDictionaryScrapper.class.getName());


    private final Clock clock;
    private final String outerSourceName = "OxfordDictionary";

    public OxfordDictionaryScrapper(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Word supplement(Word word) {
        if(word.getTranscriptionsRecentUpdateDate(outerSourceName).
                map(date -> durationGreaterThan(date, 90)).
                orElse(true)) {
            try {
                logger.info("get transcriptions for word '{}' from {}", word.getValue(), outerSourceName);
                transcriptions(word.getValue()).forEach(word::mergeTranscription);
            } catch (Exception e) {
                logger.warn("Fail to get transcriptions for word '{}' from {}. Reason: {}",
                        word.getValue(), outerSourceName, e);
            }
        }

        if(word.getInterpretationsRecentUpdateDate(outerSourceName).
                map(date -> durationGreaterThan(date, 90)).
                orElse(true)) {
            try {
                logger.info("get interpretations for word '{}' from {}", word.getValue(), outerSourceName);
                interpretations(word.getValue()).forEach(word::mergeInterpretation);
            } catch (Exception e) {
                logger.warn("Fail to get interpretations for word '{}' from {}. Reason: {}",
                        word.getValue(), outerSourceName, e);
            }
        }

        return word;
    }


    private List<WordTranscription> transcriptions(String word) throws Exception {
        Document document = Jsoup.parse(loadRawBody(toUrlOxfordDictionaryUi(word)));
        Elements elements = document.select(".phon");
        return elements.stream().
                map(Element::text).
                map(transcription -> transcription.replace("/", "")).
                map(transcription -> new WordTranscription(transcription, null).
                        addSourceInfo(
                                new OuterSource(toUrlOxfordDictionaryUi(word),
                                        outerSourceName,
                                        LocalDate.now(clock))
                        )).
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
                map(interpretation -> new WordInterpretation(interpretation).
                        addSourceInfo(
                                new OuterSource(toUrlOxfordDictionaryUi(word),
                                        outerSourceName,
                                        LocalDate.now(clock))
                        )).
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

    private boolean durationGreaterThan(LocalDate recentUpdateDate, int days) {
        return ChronoUnit.DAYS.between(recentUpdateDate, LocalDate.now(clock)) > days;
    }

    private String toUrlOxfordDictionaryUi(String word) {
        return toUrlOxfordDictionaryTemplate(word) + "_1";
    }

    private String toUrlOxfordDictionaryTemplate(String word) {
        return "https://www.oxfordlearnersdictionaries.com/definition/english/" + word;
    }

}
