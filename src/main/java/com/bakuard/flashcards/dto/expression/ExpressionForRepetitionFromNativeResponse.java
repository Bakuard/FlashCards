package com.bakuard.flashcards.dto.expression;

import com.bakuard.flashcards.dto.common.InterpretationRequestResponse;
import com.bakuard.flashcards.dto.common.TranslateRequestResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = """
        Данные выражения для списка повторяемых устойчевых выражений
         с родного языка пользователя на английский язык.
        """)
public class ExpressionForRepetitionFromNativeResponse {

    @Schema(description = "Уникальный идентификатор выражения.")
    private UUID expressionId;
    @Schema(description = "Уникальный идентификатор пользователя, к словарю которого относится это выражение.")
    private UUID userId;
    @Schema(description = "Переводы английского устойчевого выражения.")
    private List<TranslateRequestResponse> translations;
    @Schema(description = "Толкования английского устойчевого выражения.")
    private List<InterpretationRequestResponse> interpretations;

    public ExpressionForRepetitionFromNativeResponse() {

    }

    public UUID getExpressionId() {
        return expressionId;
    }

    public ExpressionForRepetitionFromNativeResponse setExpressionId(UUID expressionId) {
        this.expressionId = expressionId;
        return this;
    }

    public UUID getUserId() {
        return userId;
    }

    public ExpressionForRepetitionFromNativeResponse setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public List<TranslateRequestResponse> getTranslations() {
        return translations;
    }

    public ExpressionForRepetitionFromNativeResponse setTranslations(List<TranslateRequestResponse> translations) {
        this.translations = translations;
        return this;
    }

    public List<InterpretationRequestResponse> getInterpretations() {
        return interpretations;
    }

    public ExpressionForRepetitionFromNativeResponse setInterpretations(List<InterpretationRequestResponse> interpretations) {
        this.interpretations = interpretations;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionForRepetitionFromNativeResponse that = (ExpressionForRepetitionFromNativeResponse) o;
        return Objects.equals(expressionId, that.expressionId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(translations, that.translations) &&
                Objects.equals(interpretations, that.interpretations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expressionId, userId, translations, interpretations);
    }

    @Override
    public String toString() {
        return "ExpressionForRepetitionNativeToEnglishResponse{" +
                "wordId=" + expressionId +
                ", userId=" + userId +
                ", translations=" + translations +
                ", interpretations=" + interpretations +
                '}';
    }

}
