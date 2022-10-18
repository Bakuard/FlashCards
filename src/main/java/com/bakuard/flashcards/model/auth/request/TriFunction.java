package com.bakuard.flashcards.model.auth.request;

@FunctionalInterface
public interface TriFunction<A, B, C, R> {

    public R apply(A a, B b, C c);

}
