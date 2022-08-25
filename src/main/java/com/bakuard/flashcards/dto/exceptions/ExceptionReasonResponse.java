package com.bakuard.flashcards.dto.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Содержит данные об одной из причин ошибки запроса")
public class ExceptionReasonResponse {

    @Schema(description = "Текст сообщения пользователю об ошибке")
    private String message;

    public ExceptionReasonResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExceptionReasonResponse that = (ExceptionReasonResponse) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public String toString() {
        return "ExceptionReasonResponse{" +
                "message='" + message + '\'' +
                '}';
    }

}
