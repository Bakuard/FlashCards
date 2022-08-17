package com.bakuard.flashcards.validation;

public class UnknownEntityException extends RuntimeException {

    private String messageKey;

    public UnknownEntityException(String messageKey) {
        this.messageKey = messageKey;
    }

    public UnknownEntityException(String message, String messageKey) {
        super(message);
        this.messageKey = messageKey;
    }

    public UnknownEntityException(String message, Throwable cause, String messageKey) {
        super(message, cause);
        this.messageKey = messageKey;
    }

    public UnknownEntityException(Throwable cause, String messageKey) {
        super(cause);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

}
