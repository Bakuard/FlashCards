package com.bakuard.flashcards.dto;

import com.bakuard.flashcards.dto.common.ExampleRequestResponse;
import com.bakuard.flashcards.dto.common.InterpretationRequestResponse;
import com.bakuard.flashcards.dto.common.TranscriptionRequestResponse;
import com.bakuard.flashcards.dto.common.TranslateRequestResponse;
import com.bakuard.flashcards.dto.word.*;
import com.bakuard.flashcards.model.word.*;
import com.bakuard.flashcards.service.ExpressionService;
import com.bakuard.flashcards.service.WordService;
import com.bakuard.flashcards.validation.UnknownEntityException;

public class DtoMapper {

    private WordService wordService;
    private ExpressionService expressionService;

    public DtoMapper(WordService wordService,
                     ExpressionService expressionService) {
        this.wordService = wordService;
        this.expressionService = expressionService;
    }

    public WordResponse toWordResponse(Word word) {
        return new WordResponse().
                setWordId(word.getId()).
                setUserId(word.getUserId()).
                setValue(word.getValue()).
                setNote(word.getNote()).
                setTranscriptions(word.getTranscriptions().stream().
                        map(this::toTranscriptionRequestResponse).
                        toList()).
                setInterpretations(word.getInterpretations().stream().
                        map(this::toInterpretationRequestResponse).
                        toList()).
                setTranslates(word.getTranslations().stream().
                        map(this::toTranslateRequestResponse).
                        toList()).
                setExamples(word.getExamples().stream().
                        map(this::toExampleRequestResponse).
                        toList());
    }

    public WordForDictionaryListResponse toWordForDictionaryListResponse(Word word) {
        return new WordForDictionaryListResponse().
                setWordId(word.getId()).
                setUserId(word.getUserId()).
                setValue(word.getValue()).
                setHotRepeat(wordService.isHotRepeat(word));
    }

    public WordForRepetitionResponse toWordForRepetitionResponse(Word word) {
        return new WordForRepetitionResponse().
                setWordId(word.getId()).
                setUserId(word.getUserId()).
                setValue(word.getValue()).
                setExamples(word.getExamples().stream().
                        map(WordExample::getOrigin).
                        toList());
    }

    public Word toWord(WordAddRequest dto) {
        Word word = wordService.newWord(dto.getUserId(), dto.getValue(), dto.getNote());
        dto.getTranscriptions().forEach(t -> word.addTranscription(toWordTranscription(t)));
        dto.getInterpretations().forEach(i -> word.addInterpretation(toWordInterpretation(i)));
        dto.getTranslates().forEach(t -> word.addTranslation(toWordTranslation(t)));
        dto.getExamples().forEach(e -> word.addExample(toWordExample(e)));
        return word;
    }

    public Word toWord(WordUpdateRequest dto) {
        return wordService.findById(dto.getUserId(), dto.getWordId()).
                map(word -> {
                    word.setValue(dto.getValue()).setNote(dto.getNote());
                    dto.getTranscriptions().forEach(t -> word.addTranscription(toWordTranscription(t)));
                    dto.getInterpretations().forEach(i -> word.addInterpretation(toWordInterpretation(i)));
                    dto.getTranslates().forEach(t -> word.addTranslation(toWordTranslation(t)));
                    dto.getExamples().forEach(e -> word.addExample(toWordExample(e)));
                    return word;
                }).
                orElseThrow(
                        () -> new UnknownEntityException(
                                "Unknown word with id=" + dto.getWordId() + " userId=" + dto.getUserId(),
                                "Word.unknown"
                        )
                );
    }

    public Word toWord(WordRepeatRequest dto) {
        return wordService.findById(dto.getUserId(), dto.getWordId()).
                orElseThrow(
                        () -> new UnknownEntityException(
                                "Unknown word with id=" + dto.getWordId() + " userId=" + dto.getUserId(),
                                "Word.unknown"
                        )
                );
    }


    private ExampleRequestResponse toExampleRequestResponse(WordExample wordExample) {
        return new ExampleRequestResponse().
                setOrigin(wordExample.getOrigin()).
                setTranslate(wordExample.getTranslate()).
                setNote(wordExample.getNote());
    }

    private InterpretationRequestResponse toInterpretationRequestResponse(WordInterpretation wordInterpretation) {
        return new InterpretationRequestResponse().
                setValue(wordInterpretation.getValue());
    }

    private TranscriptionRequestResponse toTranscriptionRequestResponse(WordTranscription wordTranscription) {
        return new TranscriptionRequestResponse().
                setValue(wordTranscription.getValue()).
                setNote(wordTranscription.getNote());
    }

    private TranslateRequestResponse toTranslateRequestResponse(WordTranslation wordTranslation) {
        return new TranslateRequestResponse().
                setValue(wordTranslation.getValue()).
                setNote(wordTranslation.getNote());
    }


    private WordTranscription toWordTranscription(TranscriptionRequestResponse dto) {
        return new WordTranscription(dto.getValue(), dto.getNote());
    }

    private WordInterpretation toWordInterpretation(InterpretationRequestResponse dto) {
        return new WordInterpretation(dto.getValue());
    }

    private WordTranslation toWordTranslation(TranslateRequestResponse dto) {
        return new WordTranslation(dto.getValue(), dto.getNote());
    }

    private WordExample toWordExample(ExampleRequestResponse dto) {
        return new WordExample(dto.getOrigin(), dto.getTranslate(), dto.getNote());
    }

}
