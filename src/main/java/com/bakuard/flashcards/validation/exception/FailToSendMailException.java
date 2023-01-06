package com.bakuard.flashcards.validation.exception;

public class FailToSendMailException extends AbstractDomainException {

    public FailToSendMailException(String message, String messageKey) {
        super(message, messageKey);
    }

    public FailToSendMailException(String message, String messageKey, boolean internalServerException) {
        super(message, messageKey, internalServerException);
    }

    public FailToSendMailException(String message, Throwable cause, String messageKey, boolean internalServerException) {
        super(message, cause, messageKey, internalServerException);
    }

}
