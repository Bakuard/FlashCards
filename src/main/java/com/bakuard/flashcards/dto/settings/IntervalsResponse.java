package com.bakuard.flashcards.dto.settings;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Все интервалы повторения кокретного пользователя.")
public class IntervalsResponse {

    @Schema(description = "Уникальный идентификатор пользователя.")
    private UUID userId;
    @Schema(description = "Все интервалы повторения конкретного пользователя.")
    private List<Integer> intervals;

    public IntervalsResponse() {

    }

    public UUID getUserId() {
        return userId;
    }

    public IntervalsResponse setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public List<Integer> getIntervals() {
        return intervals;
    }

    public IntervalsResponse setIntervals(List<Integer> intervals) {
        this.intervals = intervals;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntervalsResponse that = (IntervalsResponse) o;
        return Objects.equals(userId, that.userId) && Objects.equals(intervals, that.intervals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, intervals);
    }

    @Override
    public String toString() {
        return "IntervalsResponse{" +
                "userId=" + userId +
                ", intervals=" + intervals +
                '}';
    }

}
