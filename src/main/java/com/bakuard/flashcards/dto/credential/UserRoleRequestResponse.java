package com.bakuard.flashcards.dto.credential;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Данные об одной конкретной роли некотоого пользователя.")
public class UserRoleRequestResponse {

    @Schema(description = "Наименование роли.")
    private String name;

    public UserRoleRequestResponse() {

    }

    public String getName() {
        return name;
    }

    public UserRoleRequestResponse setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRoleRequestResponse that = (UserRoleRequestResponse) o;
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
