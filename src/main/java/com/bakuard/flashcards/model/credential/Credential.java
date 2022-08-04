package com.bakuard.flashcards.model.credential;

import javax.validation.Valid;
import java.util.Objects;

public class Credential {

    @Valid
    private RawPassword rawPassword;
    @Valid
    private Email email;

    public Credential(RawPassword rawPassword, Email email) {
        this.rawPassword = rawPassword;
        this.email = email;
    }

    public Credential(String rawPassword, String email) {
        this.rawPassword = new RawPassword(rawPassword);
        this.email = new Email(email);
    }

    public RawPassword getRawPassword() {
        return rawPassword;
    }

    public Email getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Credential that = (Credential) o;
        return Objects.equals(rawPassword, that.rawPassword) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawPassword, email);
    }

    @Override
    public String toString() {
        return "Credential{" + rawPassword + ", " + email + '}';
    }

}
