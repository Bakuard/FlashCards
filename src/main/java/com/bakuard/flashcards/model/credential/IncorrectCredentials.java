package com.bakuard.flashcards.model.credential;

public class IncorrectCredentials extends RuntimeException {

    public IncorrectCredentials() {
    }

    public IncorrectCredentials(String message) {
        super(message);
    }

    public IncorrectCredentials(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectCredentials(Throwable cause) {
        super(cause);
    }

}
