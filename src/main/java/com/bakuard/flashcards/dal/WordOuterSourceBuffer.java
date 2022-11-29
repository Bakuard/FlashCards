package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.model.word.Word;

public interface WordOuterSourceBuffer {

    public void saveDataFromOuterSource(Word word);

    public void mergeFromOuterSource(Word word);

    public void deleteUnusedOuterSourceExamples();

}
