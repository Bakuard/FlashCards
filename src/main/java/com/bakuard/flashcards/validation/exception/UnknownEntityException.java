package com.bakuard.flashcards.validation.exception;

public class UnknownEntityException extends AbstractDomainException {

    public UnknownEntityException(String message, String messageKey) {
        super(message, messageKey);
    }

    public UnknownEntityException(String message, String messageKey, boolean internalServerException) {
        super(message, messageKey, internalServerException);
    }

    public UnknownEntityException(String message, Throwable cause, String messageKey, boolean internalServerException) {
        super(message, cause, messageKey, internalServerException);
    }

}
