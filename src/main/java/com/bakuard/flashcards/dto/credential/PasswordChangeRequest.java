package com.bakuard.flashcards.dto.credential;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Запрос на изменение пароля.")
public class PasswordChangeRequest {

    @Schema(description = """
            Текущий пароль пользователя. <br/>
             Ограничения: Не должен быть null.
            """)
    private String currentPassword;
    @Schema(description = """
            Новый пароль пользователя. <br/>
             Ограничения: <br/>
             1. Не должен быть null. <br/>
             2. Должен содержать минимум 8 символов. <br/>
             3. Должен содержать хотя бы один отображаемый символ. <br/>
             4. Не должен превышать по длине 50 символов.
            """)
    private String newPassword;

    public PasswordChangeRequest() {

    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public PasswordChangeRequest setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
        return this;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public PasswordChangeRequest setNewPassword(String newPassword) {
        this.newPassword = newPassword;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordChangeRequest that = (PasswordChangeRequest) o;
        return Objects.equals(currentPassword, that.currentPassword) &&
                Objects.equals(newPassword, that.newPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentPassword, newPassword);
    }

    @Override
    public String toString() {
        return "PasswordChangeRequest{" +
                "currentPassword='" + currentPassword + '\'' +
                ", newPassword='" + newPassword + '\'' +
                '}';
    }

}
