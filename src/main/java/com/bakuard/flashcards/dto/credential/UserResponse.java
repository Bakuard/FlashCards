package com.bakuard.flashcards.dto.credential;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Возвращаемые учетные данные пользователя.")
public class UserResponse {

    @Schema(description = "Уникальный идентификатор поьзователя.")
    private UUID userId;
    @Schema(description = "Адрес электронной почты пользователя.")
    private String email;
    @Schema(description = "Роли пользователя.")
    private List<UserRoleRequestResponse> roles;

    public UserResponse() {

    }

    public UUID getUserId() {
        return userId;
    }

    public UserResponse setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserResponse setEmail(String email) {
        this.email = email;
        return this;
    }

    public List<UserRoleRequestResponse> getRoles() {
        return roles;
    }

    public UserResponse setRoles(List<UserRoleRequestResponse> roles) {
        this.roles = roles;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserResponse that = (UserResponse) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(email, that.email) &&
                Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email, roles);
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                '}';
    }

}
