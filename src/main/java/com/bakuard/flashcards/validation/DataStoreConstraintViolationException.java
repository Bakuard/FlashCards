package com.bakuard.flashcards.validation;

public class DataStoreConstraintViolationException extends RuntimeException {

    private String messageKey;

    public DataStoreConstraintViolationException(String messageKey) {
        this.messageKey = messageKey;
    }

    public DataStoreConstraintViolationException(String message, String messageKey) {
        super(message);
        this.messageKey = messageKey;
    }

    public DataStoreConstraintViolationException(String message, Throwable cause, String messageKey) {
        super(message, cause);
        this.messageKey = messageKey;
    }

    public DataStoreConstraintViolationException(Throwable cause, String messageKey) {
        super(cause);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

}
