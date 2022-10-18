package com.bakuard.flashcards.dto.settings;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = "Данные запроса для изменения интервала повторения.")
public class IntervalReplaceRequest {

    @Schema(description = """
            Уникальный идентификатор пользователя для которого добавляется интервал повторения. <br/>
            Ограничения: не должен быть null.
            """)
    private UUID userId;
    @Schema(description = """
            Заменяемый интервал повторения. <br/>
            Ограничения: у поьзователя должен быть данный интервал повторения.
            """)
    private int oldInterval;
    @Schema(description = """
            Новый интервал повторения для заменяемого. <br/>
            Ограничения: добавляемый интервал должен быть уникальным для данного пользователя.
            """)
    private int newInterval;

    public IntervalReplaceRequest() {

    }

    public UUID getUserId() {
        return userId;
    }

    public IntervalReplaceRequest setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public int getOldInterval() {
        return oldInterval;
    }

    public IntervalReplaceRequest setOldInterval(int oldInterval) {
        this.oldInterval = oldInterval;
        return this;
    }

    public int getNewInterval() {
        return newInterval;
    }

    public IntervalReplaceRequest setNewInterval(int newInterval) {
        this.newInterval = newInterval;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntervalReplaceRequest that = (IntervalReplaceRequest) o;
        return oldInterval == that.oldInterval &&
                newInterval == that.newInterval &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, oldInterval, newInterval);
    }

    @Override
    public String toString() {
        return "IntervalReplaceRequest{" +
                "userId=" + userId +
                ", oldInterval=" + oldInterval +
                ", newInterval=" + newInterval +
                '}';
    }

}
