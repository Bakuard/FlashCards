package com.bakuard.flashcards.dal.impl.fragment;

import com.bakuard.flashcards.model.word.Word;

public interface WordOuterSourceBuffer<T> {

    public T save(T word);

    public void saveDataFromOuterSourceExcludeExamples(Word word);

    public void mergeFromOuterSource(Word word);

}
