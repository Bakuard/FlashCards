package com.bakuard.flashcards.model.credential;

import javax.validation.constraints.NotNull;
import java.util.Objects;

class Email {

    @NotNull(message = "Email.notNull")
    @javax.validation.constraints.Email(message = "Email.format")
    private String email;

    Email(String email) {
        this.email = email;
    }

    public String asString() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email1 = (Email) o;
        return Objects.equals(email, email1.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "Email{" + email + '}';
    }

}
