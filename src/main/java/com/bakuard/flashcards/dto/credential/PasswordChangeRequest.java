package com.bakuard.flashcards.dto.credential;

import java.util.Objects;

public class PasswordChangeRequest {

    private String currentPassword;
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
