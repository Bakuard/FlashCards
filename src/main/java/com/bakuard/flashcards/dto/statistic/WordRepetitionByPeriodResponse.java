package com.bakuard.flashcards.dto.statistic;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = "Статистические данные о результатах повторения слова.")
public class WordRepetitionByPeriodResponse {

    @Schema(description = "Уникальный идентификатор пользователя.")
    private UUID userId;
    @Schema(description = "Уникальный идентификатор слова.")
    private UUID wordId;
    @Schema(description = "Значение слова.")
    private String value;
    @Schema(description = "Кол-во успешных повторений перевода слова с английского на родной язык пользователя.")
    private long rememberFromEnglish;
    @Schema(description = "Кол-во не успешных повторений перевода слова с английского на родной язык пользователя.")
    private long notRememberFromEnglish;
    @Schema(description = "Кол-во успешных повторений перевода слова с родного языка пользователя на ангийский.")
    private long rememberFromNative;
    @Schema(description = "Кол-во не успешных повторений перевода слова с родного языка пользователя на ангийский.")
    private long notRememberFromNative;
    @Schema(description = "Общее кол-во повторений слова с английского языка на родной язык пользователя.")
    private long totalRepetitionNumbersFromEnglish;
    @Schema(description = "Общее кол-во повторений слова с родного языка пользователя на английский.")
    private long totalRepetitionNumbersFromNative;

    public WordRepetitionByPeriodResponse() {

    }

    public UUID getUserId() {
        return userId;
    }

    public WordRepetitionByPeriodResponse setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public UUID getWordId() {
        return wordId;
    }

    public WordRepetitionByPeriodResponse setWordId(UUID wordId) {
        this.wordId = wordId;
        return this;
    }

    public String getValue() {
        return value;
    }

    public WordRepetitionByPeriodResponse setValue(String value) {
        this.value = value;
        return this;
    }

    public long getRememberFromEnglish() {
        return rememberFromEnglish;
    }

    public WordRepetitionByPeriodResponse setRememberFromEnglish(long rememberFromEnglish) {
        this.rememberFromEnglish = rememberFromEnglish;
        return this;
    }

    public long getNotRememberFromEnglish() {
        return notRememberFromEnglish;
    }

    public WordRepetitionByPeriodResponse setNotRememberFromEnglish(long notRememberFromEnglish) {
        this.notRememberFromEnglish = notRememberFromEnglish;
        return this;
    }

    public long getRememberFromNative() {
        return rememberFromNative;
    }

    public WordRepetitionByPeriodResponse setRememberFromNative(long rememberFromNative) {
        this.rememberFromNative = rememberFromNative;
        return this;
    }

    public long getNotRememberFromNative() {
        return notRememberFromNative;
    }

    public WordRepetitionByPeriodResponse setNotRememberFromNative(long notRememberFromNative) {
        this.notRememberFromNative = notRememberFromNative;
        return this;
    }

    public long getTotalRepetitionNumbersFromEnglish() {
        return totalRepetitionNumbersFromEnglish;
    }

    public WordRepetitionByPeriodResponse setTotalRepetitionNumbersFromEnglish(long totalRepetitionNumbersFromEnglish) {
        this.totalRepetitionNumbersFromEnglish = totalRepetitionNumbersFromEnglish;
        return this;
    }

    public long getTotalRepetitionNumbersFromNative() {
        return totalRepetitionNumbersFromNative;
    }

    public WordRepetitionByPeriodResponse setTotalRepetitionNumbersFromNative(long totalRepetitionNumbersFromNative) {
        this.totalRepetitionNumbersFromNative = totalRepetitionNumbersFromNative;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordRepetitionByPeriodResponse that = (WordRepetitionByPeriodResponse) o;
        return rememberFromEnglish == that.rememberFromEnglish &&
                notRememberFromEnglish == that.notRememberFromEnglish &&
                rememberFromNative == that.rememberFromNative &&
                notRememberFromNative == that.notRememberFromNative &&
                totalRepetitionNumbersFromEnglish == that.totalRepetitionNumbersFromEnglish &&
                totalRepetitionNumbersFromNative == that.totalRepetitionNumbersFromNative &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(wordId, that.wordId) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, wordId, value, rememberFromEnglish,
                notRememberFromEnglish, rememberFromNative, notRememberFromNative,
                totalRepetitionNumbersFromEnglish, totalRepetitionNumbersFromNative);
    }

    @Override
    public String toString() {
        return "WordRepetitionByPeriodResponse{" +
                "userId=" + userId +
                ", wordId=" + wordId +
                ", value='" + value + '\'' +
                ", rememberFromEnglish=" + rememberFromEnglish +
                ", notRememberFromEnglish=" + notRememberFromEnglish +
                ", rememberFromNative=" + rememberFromNative +
                ", notRememberFromNative=" + notRememberFromNative +
                ", totalRepetitionNumbersFromEnglish=" + totalRepetitionNumbersFromEnglish +
                ", totalRepetitionNumbersFromNative=" + totalRepetitionNumbersFromNative +
                '}';
    }

}
