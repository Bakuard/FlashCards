package com.bakuard.flashcards.validation.exception;

public class InvalidParameter extends AbstractDomainException {

    public InvalidParameter(String message, String messageKey) {
        super(message, messageKey);
    }

    public InvalidParameter(String message, String messageKey, boolean internalServerException) {
        super(message, messageKey, internalServerException);
    }

    public InvalidParameter(String message, Throwable cause, String messageKey, boolean internalServerException) {
        super(message, cause, messageKey, internalServerException);
    }

}
