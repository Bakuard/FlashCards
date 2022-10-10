package com.bakuard.flashcards.dto.credential;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UserUpdateRequest {

    private UUID userId;
    private String email;
    private List<UserRoleResponse> roles;
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

    public List<UserRoleResponse> getRoles() {
        return roles;
    }

    public UserUpdateRequest setRoles(List<UserRoleResponse> roles) {
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
