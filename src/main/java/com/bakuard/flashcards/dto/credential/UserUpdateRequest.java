package com.bakuard.flashcards.dto.credential;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Данные запроса на обновление данных пользователя.")
public class UserUpdateRequest {

    @Schema(description = """
            Униклаьный идентификатор пользователя. <br/>
             Ограничения: не должен быть null.
            """)
    private UUID userId;
    @Schema(description = """
            Уникальный идентификатор пользователя. <br/>
             Ограничения: <br/>
             1. не должен быть null. <br/>
             2. заданное значение должно представлять корректный адрес электронной почты.
            """)
    private String email;
    @Schema(description = """
            Роли пользователя. Может принимать значение null. <br/>
             Ограничения: <br/>
             1. Не должно содержать значений null. <br/>
             2. Не должно содержать дубликатов. <br/>
             3. Наименование каждой роли должно содержать хотя бы один отображаемый символ.
            """)
    private List<UserRoleRequestResponse> roles;
    @Schema(description = "Запрос на изменение пароля. Если равен null - пароль пользователя не меняется.")
    private PasswordChangeRequest passwordChangeRequest;

    public UserUpdateRequest() {

    }

    public UUID getUserId() {
        return userId;
    }

    public UserUpdateRequest setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserUpdateRequest setEmail(String email) {
        this.email = email;
        return this;
    }

    public List<UserRoleRequestResponse> getRoles() {
        return roles;
    }

    public UserUpdateRequest setRoles(List<UserRoleRequestResponse> roles) {
        this.roles = roles;
        return this;
    }

    public PasswordChangeRequest getPasswordChangeRequest() {
        return passwordChangeRequest;
    }

    public UserUpdateRequest setPasswordChangeRequest(PasswordChangeRequest passwordChangeRequest) {
        this.passwordChangeRequest = passwordChangeRequest;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserUpdateRequest that = (UserUpdateRequest) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(email, that.email) &&
                Objects.equals(roles, that.roles) &&
                Objects.equals(passwordChangeRequest, that.passwordChangeRequest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email, roles, passwordChangeRequest);
    }

    @Override
    public String toString() {
        return "UserUpdateRequest{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                ", passwordChangeRequest=" + passwordChangeRequest +
                '}';
    }

}
