package com.bakuard.flashcards.dal.impl.fragment;

import com.bakuard.flashcards.model.word.Word;

public interface WordOuterSourceBuffer<T> {

    public void saveDataFromOuterSource(Word word);

    public void mergeFromOuterSource(Word word);

    public void deleteUnusedOuterSourceExamples();

}
