package com.bakuard.flashcards.dto.credential;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Данные запроса на востановление учетных данных пользователя.")
public class PasswordRestoreRequest {

    @Schema(description = """
            Адрес электроной почты пользователя. <br/>
             Ограничения: <br/>
             1. не должен быть null. <br/>
             2. заданное значение должно представлять корректный адрес электронной почты.
            """)
    private String email;
    @Schema(description = """
            Новый пароль пользователя. <br/>
             Ограничения: <br/>
             1. Не должен быть null. <br/>
             2. Должен содержать минимум 8 символов. <br/>
             3. Должен содержать хотя бы один отображаемый символ. <br/>
             4. Не должен превышать по длине 50 символов.
            """)
    private String newPassword;

    public PasswordRestoreRequest() {

    }

    public String getEmail() {
        return email;
    }

    public PasswordRestoreRequest setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public PasswordRestoreRequest setNewPassword(String newPassword) {
        this.newPassword = newPassword;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordRestoreRequest that = (PasswordRestoreRequest) o;
        return Objects.equals(email, that.email) &&
                Objects.equals(newPassword, that.newPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, newPassword);
    }

    @Override
    public String toString() {
        return "PasswordRestoreRequest{" +
                "email='" + email + '\'' +
                ", newPassword='" + newPassword + '\'' +
                '}';
    }

}
