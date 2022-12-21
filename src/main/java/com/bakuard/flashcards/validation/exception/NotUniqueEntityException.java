package com.bakuard.flashcards.validation.exception;

public class NotUniqueEntityException extends AbstractDomainException {

    public NotUniqueEntityException(String message, String messageKey) {
        super(message, messageKey);
    }

    public NotUniqueEntityException(String message, String messageKey, boolean internalServerException) {
        super(message, messageKey, internalServerException);
    }

    public NotUniqueEntityException(String message, Throwable cause, String messageKey, boolean internalServerException) {
        super(message, cause, messageKey, internalServerException);
    }

}
