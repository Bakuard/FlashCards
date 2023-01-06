package com.bakuard.flashcards.validation.exception;

public abstract class AbstractDomainException extends RuntimeException {

    private String messageKey;
    private boolean internalServerException;

    public AbstractDomainException(String message, String messageKey) {
        super(message);
        this.messageKey = messageKey;
    }

    public AbstractDomainException(String message, String messageKey, boolean internalServerException) {
        super(message);
        this.messageKey = messageKey;
        this.internalServerException = internalServerException;
    }

    public AbstractDomainException(String message, Throwable cause, String messageKey, boolean internalServerException) {
        super(message, cause);
        this.messageKey = messageKey;
        this.internalServerException = internalServerException;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public boolean isInternalServerException() {
        return internalServerException;
    }

    public boolean isUserLevelException() {
        return !internalServerException;
    }

}
