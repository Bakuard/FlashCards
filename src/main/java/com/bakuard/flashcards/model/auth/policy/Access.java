package com.bakuard.flashcards.model.auth.policy;

public enum Access {

    ACCEPT(1),
    UNKNOWN(0),
    DENY(-1);


    private final int level;

    private Access(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

}
