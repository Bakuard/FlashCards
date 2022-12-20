package com.bakuard.flashcards.validation;

public class NotUniqueEntityException extends RuntimeException {

    private String messageKey;
    private boolean internalServerException;

    public NotUniqueEntityException(String message, String messageKey) {
        super(message);
        this.messageKey = messageKey;
    }

    public NotUniqueEntityException(String message, String messageKey, boolean internalServerException) {
        super(message);
        this.messageKey = messageKey;
        this.internalServerException = internalServerException;
    }

    public NotUniqueEntityException(Throwable cause, String messageKey) {
        super(cause);
        this.messageKey = messageKey;
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
