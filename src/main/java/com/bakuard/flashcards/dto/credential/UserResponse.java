package com.bakuard.flashcards.dto.credential;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UserResponse {

    private UUID userId;
    private String email;
    private List<UserRoleResponse> roles;

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

    public List<UserRoleResponse> getRoles() {
        return roles;
    }

    public UserResponse setRoles(List<UserRoleResponse> roles) {
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
