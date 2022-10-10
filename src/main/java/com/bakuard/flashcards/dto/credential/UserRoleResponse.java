package com.bakuard.flashcards.dto.credential;

import java.util.Objects;

public class UserRoleResponse {

    private String name;

    public UserRoleResponse() {

    }

    public String getName() {
        return name;
    }

    public UserRoleResponse setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRoleResponse that = (UserRoleResponse) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "UserRoleResponse{" +
                "name='" + name + '\'' +
                '}';
    }

}
