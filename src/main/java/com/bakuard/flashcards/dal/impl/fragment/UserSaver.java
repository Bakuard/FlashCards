package com.bakuard.flashcards.dal.impl.fragment;

public interface UserSaver<T> {

    public T save(T user);

}
