package com.bakuard.flashcards.validation.exception;

public class IncorrectCredentials extends AbstractDomainException {

    public IncorrectCredentials(String message, String messageKey) {
        super(message, messageKey);
    }

    public IncorrectCredentials(String message, String messageKey, boolean internalServerException) {
        super(message, messageKey, internalServerException);
    }

    public IncorrectCredentials(String message, Throwable cause, String messageKey, boolean internalServerException) {
        super(message, cause, messageKey, internalServerException);
    }

}
