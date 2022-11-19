package com.bakuard.flashcards.dal.impl.fragment;

import com.bakuard.flashcards.model.word.WordExample;
import com.bakuard.flashcards.model.word.WordInterpretation;
import com.bakuard.flashcards.model.word.WordTranscription;
import com.bakuard.flashcards.model.word.WordTranslation;

import java.util.List;
import java.util.UUID;

public interface WordSourceInfo<T> {

    public T save(T word);

    public List<WordTranscription> getTranscriptionsFromOuterSourceFor(String wordValue);

    public List<WordInterpretation> getInterpretationsFromOuterSourceFor(String wordValue);

    public List<WordTranslation> getTranslationsFromOuterSourceFor(String wordValue);

    public List<WordExample> getExamplesFromOuterSourceFor(UUID wordId);

    public void saveTranscriptionsToBuffer(String wordValue, List<WordTranscription> transcriptions);

    public void saveInterpretationsToBuffer(String wordValue, List<WordInterpretation> interpretations);

    public void saveTranslationsToBuffer(String wordValue, List<WordTranslation> translations);

}
