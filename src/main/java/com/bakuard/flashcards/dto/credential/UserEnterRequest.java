package com.bakuard.flashcards.dto.credential;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Учетные данные пользователя для входа.")
public class UserEnterRequest {

    @Schema(description = """
            Адрес электронной почты пользователя. <br/>
             Ограничения: <br/>
             1. не должен быть null. <br/>
             2. заданное значение должно представлять корректный адрес электронной почты.
            """)
    private String email;
    @Schema(description = """
            Пароль пользователя. <br/>
             Ограничения: не должен быть null.
            """)
    private String password;

    public UserEnterRequest() {

    }

    public String getEmail() {
        return email;
    }

    public UserEnterRequest setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserEnterRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEnterRequest that = (UserEnterRequest) o;
        return Objects.equals(email, that.email) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password);
    }

    @Override
    public String toString() {
        return "UserEnterRequest{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

}
