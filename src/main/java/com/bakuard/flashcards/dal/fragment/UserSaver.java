package com.bakuard.flashcards.dal.fragment;

public interface UserSaver<T> {

    public T save(T user);

}
