package com.bakuard.flashcards.dto.credential;

import java.util.Objects;

public class ResponseMessage<T> {

    private String message;
    private T payload;

    public ResponseMessage() {

    }

    public String getMessage() {
        return message;
    }

    public ResponseMessage<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getPayload() {
        return payload;
    }

    public ResponseMessage<T> setPayload(T payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResponseMessage<?> that = (ResponseMessage<?>) o;
        return Objects.equals(message, that.message) && Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, payload);
    }

    @Override
    public String toString() {
        return "ResponseMessage{" +
                "message='" + message + '\'' +
                ", payload=" + payload +
                '}';
    }

}
