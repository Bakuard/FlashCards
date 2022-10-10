package com.bakuard.flashcards.dto.credential;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Данные запроса на добавление данных пользователя.")
public class UserAddRequest {

    @Schema(description = """
            Адрес электроной почты пользователя. <br/>
             Ограничения: <br/>
             1. не должен быть null. <br/>
             2. заданное значение должно представлять корректный адрес электронной почты.
            """)
    private String email;
    @Schema(description = """
            Пароль пользователя. <br/>
             Ограничения: <br/>
             1. Не должен быть null. <br/>
             2. Должен содержать минимум 8 символов. <br/>
             3. Должен содержать хотя бы один отображаемый символ. <br/>
             4. Не должен превышать по длине 50 символов.
            """)
    private String password;

    public UserAddRequest() {

    }

    public String getEmail() {
        return email;
    }

    public UserAddRequest setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserAddRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAddRequest that = (UserAddRequest) o;
        return Objects.equals(email, that.email) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password);
    }

    @Override
    public String toString() {
        return "UserAddRequest{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

}
