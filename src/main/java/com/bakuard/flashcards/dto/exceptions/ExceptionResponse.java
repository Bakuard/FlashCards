package com.bakuard.flashcards.dto.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Schema(description = "Содержит данные обо всех ошибках возникших в процессе выполнения конкретного запроса")
public class ExceptionResponse {

    @Schema(description = "Время возникновения ошибки")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss(nn)")
    private LocalDateTime timestamp;
    @Schema(description = "Http статус ошибки")
    private String httpStatus;
    @Schema(description = "Http код ошибки")
    private int httpErrorCode;
    @Schema(description = "Все пречины из-за которых запрос не смог завершится корректно")
    private List<ExceptionReasonResponse> reasons;

    public ExceptionResponse(HttpStatus status, Clock clock) {
        this.timestamp = LocalDateTime.now(clock);
        httpStatus = status.getReasonPhrase();
        httpErrorCode = status.value();
        reasons = new ArrayList<>();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getHttpStatus() {
        return httpStatus;
    }

    public int getHttpErrorCode() {
        return httpErrorCode;
    }

    public List<ExceptionReasonResponse> getReasons() {
        return reasons;
    }

    public ExceptionResponse addReason(ExceptionReasonResponse reason) {
        reasons.add(reason);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExceptionResponse that = (ExceptionResponse) o;
        return httpErrorCode == that.httpErrorCode &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(httpStatus, that.httpStatus) &&
                Objects.equals(reasons, that.reasons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, httpStatus, httpErrorCode, reasons);
    }

    @Override
    public String toString() {
        return "ExceptionResponse{" +
                "timestamp=" + timestamp +
                ", httpStatus='" + httpStatus + '\'' +
                ", httpErrorCode=" + httpErrorCode +
                ", reasons=" + reasons +
                '}';
    }

}
