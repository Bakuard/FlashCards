package com.bakuard.flashcards.dto.credential;

import java.util.Objects;

public class UserEnterRequest {

    private String email;
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
