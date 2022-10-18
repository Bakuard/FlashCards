package com.bakuard.flashcards.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Результат повторения слова ил устойчевого выражения")
public class RepetitionResponse<T> {

    @Schema(description = """
            Указывает результат повторения: смог пользователь вспомнить слово/выражение или нет.
            """)
    private boolean isRemember;
    @Schema(description = "Повторяемое слово или выражение.")
    private T payload;

    public RepetitionResponse() {

    }

    public boolean isRemember() {
        return isRemember;
    }

    public RepetitionResponse<T> setRemember(boolean remember) {
        isRemember = remember;
        return this;
    }

    public T getPayload() {
        return payload;
    }

    public RepetitionResponse<T> setPayload(T payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepetitionResponse<?> that = (RepetitionResponse<?>) o;
        return isRemember == that.isRemember && Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isRemember, payload);
    }

    @Override
    public String toString() {
        return "RepetitionResponse{" +
                "isRemember=" + isRemember +
                ", payload=" + payload +
                '}';
    }

}
