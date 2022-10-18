package com.bakuard.flashcards.model.auth.resource;

public class IncorrectParamException extends RuntimeException {

    public IncorrectParamException() {
    }

    public IncorrectParamException(String message) {
        super(message);
    }

    public IncorrectParamException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectParamException(Throwable cause) {
        super(cause);
    }

}
