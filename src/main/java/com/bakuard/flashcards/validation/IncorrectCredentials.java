package com.bakuard.flashcards.validation;

public class IncorrectCredentials extends RuntimeException {

    private String messageKey;

    public IncorrectCredentials(String messageKey) {
        this.messageKey = messageKey;
    }

    public IncorrectCredentials(String message, String messageKey) {
        super(message);
        this.messageKey = messageKey;
    }

    public IncorrectCredentials(String message, Throwable cause, String messageKey) {
        super(message, cause);
        this.messageKey = messageKey;
    }

    public IncorrectCredentials(Throwable cause, String messageKey) {
        super(cause);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

}
