package com.bakuard.flashcards.validation;

public class FailToSendMailException extends RuntimeException {

    private String messageKey;

    public FailToSendMailException(String messageKey) {
        this.messageKey = messageKey;
    }

    public FailToSendMailException(String message, String messageKey) {
        super(message);
        this.messageKey = messageKey;
    }

    public FailToSendMailException(String message, Throwable cause, String messageKey) {
        super(message, cause);
        this.messageKey = messageKey;
    }

    public FailToSendMailException(Throwable cause, String messageKey) {
        super(cause);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

}
