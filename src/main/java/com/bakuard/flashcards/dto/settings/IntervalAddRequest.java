package com.bakuard.flashcards.dto.settings;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = "Данные запроса на добавления нового интервала повторения.")
public class IntervalAddRequest {

    @Schema(description = """
            Уникальный идентификатор пользователя для которого добавляется интервал повторения. <br/>
            Ограничения: не должен быть null.
            """)
    private UUID userId;
    @Schema(description = """
            Добавляемый интервал повторения. <br/>
            Ограничения: добавляемый интервал должен быть уникальным для данного пользователя.
            """)
    private int interval;

    public IntervalAddRequest() {

    }

    public UUID getUserId() {
        return userId;
    }

    public IntervalAddRequest setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public int getInterval() {
        return interval;
    }

    public IntervalAddRequest setInterval(int interval) {
        this.interval = interval;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntervalAddRequest that = (IntervalAddRequest) o;
        return interval == that.interval && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, interval);
    }

    @Override
    public String toString() {
        return "IntervalAddRequest{" +
                "userId=" + userId +
                ", interval=" + interval +
                '}';
    }

}
