package com.bakuard.flashcards.dto.statistic;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = "Статистические данные о результатах повторения устойчевого выражения.")
public class ExpressionRepetitionByPeriodResponse {

    @Schema(description = "Уникальный идентификатор пользователя.")
    private UUID userId;
    @Schema(description = "Уникальный идентификатор устойчевого выражения.")
    private UUID expressionId;
    @Schema(description = "Значение устойчевого выражения.")
    private String value;
    @Schema(description = "Кол-во успешных повторений перевода выражения с английского на родной язык пользователя.")
    private long rememberFromEnglish;
    @Schema(description = "Кол-во не успешных повторений перевода выражения с английского на родной язык пользователя.")
    private long notRememberFromEnglish;
    @Schema(description = "Кол-во успешных повторений перевода выражения с родного языка пользователя на ангийский.")
    private long rememberFromNative;
    @Schema(description = "Кол-во не успешных повторений перевода выражения с родного языка пользователя на ангийский.")
    private long notRememberFromNative;
    @Schema(description = "Общее кол-во повторений выражения с английского языка на родной язык пользователя.")
    private long totalRepetitionNumbersFromEnglish;
    @Schema(description = "Общее кол-во повторений выражения с родного языка пользователя на английский.")
    private long totalRepetitionNumbersFromNative;

    public ExpressionRepetitionByPeriodResponse() {

    }

    public UUID getUserId() {
        return userId;
    }

    public ExpressionRepetitionByPeriodResponse setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public UUID getExpressionId() {
        return expressionId;
    }

    public ExpressionRepetitionByPeriodResponse setExpressionId(UUID expressionId) {
        this.expressionId = expressionId;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ExpressionRepetitionByPeriodResponse setValue(String value) {
        this.value = value;
        return this;
    }

    public long getRememberFromEnglish() {
        return rememberFromEnglish;
    }

    public ExpressionRepetitionByPeriodResponse setRememberFromEnglish(long rememberFromEnglish) {
        this.rememberFromEnglish = rememberFromEnglish;
        return this;
    }

    public long getNotRememberFromEnglish() {
        return notRememberFromEnglish;
    }

    public ExpressionRepetitionByPeriodResponse setNotRememberFromEnglish(long notRememberFromEnglish) {
        this.notRememberFromEnglish = notRememberFromEnglish;
        return this;
    }

    public long getRememberFromNative() {
        return rememberFromNative;
    }

    public ExpressionRepetitionByPeriodResponse setRememberFromNative(long rememberFromNative) {
        this.rememberFromNative = rememberFromNative;
        return this;
    }

    public long getNotRememberFromNative() {
        return notRememberFromNative;
    }

    public ExpressionRepetitionByPeriodResponse setNotRememberFromNative(long notRememberFromNative) {
        this.notRememberFromNative = notRememberFromNative;
        return this;
    }

    public long getTotalRepetitionNumbersFromEnglish() {
        return totalRepetitionNumbersFromEnglish;
    }

    public ExpressionRepetitionByPeriodResponse setTotalRepetitionNumbersFromEnglish(long totalRepetitionNumbersFromEnglish) {
        this.totalRepetitionNumbersFromEnglish = totalRepetitionNumbersFromEnglish;
        return this;
    }

    public long getTotalRepetitionNumbersFromNative() {
        return totalRepetitionNumbersFromNative;
    }

    public ExpressionRepetitionByPeriodResponse setTotalRepetitionNumbersFromNative(long totalRepetitionNumbersFromNative) {
        this.totalRepetitionNumbersFromNative = totalRepetitionNumbersFromNative;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionRepetitionByPeriodResponse that = (ExpressionRepetitionByPeriodResponse) o;
        return rememberFromEnglish == that.rememberFromEnglish &&
                notRememberFromEnglish == that.notRememberFromEnglish &&
                rememberFromNative == that.rememberFromNative &&
                notRememberFromNative == that.notRememberFromNative &&
                totalRepetitionNumbersFromEnglish == that.totalRepetitionNumbersFromEnglish &&
                totalRepetitionNumbersFromNative == that.totalRepetitionNumbersFromNative &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(expressionId, that.expressionId) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, expressionId, value, rememberFromEnglish,
                notRememberFromEnglish, rememberFromNative, notRememberFromNative,
                totalRepetitionNumbersFromEnglish, totalRepetitionNumbersFromNative);
    }

    @Override
    public String toString() {
        return "ExpressionRepetitionByPeriodResponse{" +
                "userId=" + userId +
                ", expressionId=" + expressionId +
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
