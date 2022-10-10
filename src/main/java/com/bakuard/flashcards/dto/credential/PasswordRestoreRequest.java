package com.bakuard.flashcards.dto.credential;

import java.util.Objects;

public class PasswordRestoreRequest {

    private String email;
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
