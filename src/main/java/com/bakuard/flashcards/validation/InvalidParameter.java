package com.bakuard.flashcards.validation;

public class InvalidParameter extends RuntimeException {

    private String messageKey;

    public InvalidParameter(String messageKey) {
        this.messageKey = messageKey;
    }

    public InvalidParameter(String message, String messageKey) {
        super(message);
        this.messageKey = messageKey;
    }

    public InvalidParameter(String message, Throwable cause, String messageKey) {
        super(message, cause);
        this.messageKey = messageKey;
    }

    public InvalidParameter(Throwable cause, String messageKey) {
        super(cause);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

}
