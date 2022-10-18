package com.bakuard.flashcards.dto.credential;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Вовращаемый токен доступа.")
public class JwsResponse {

    @Schema(description = "Токен доступа.")
    private String jws;
    @Schema(description = "Пользователь, над данными которого выполянется операция.")
    private UserResponse user;

    public JwsResponse() {

    }

    public String getJws() {
        return jws;
    }

    public JwsResponse setJws(String jws) {
        this.jws = jws;
        return this;
    }

    public UserResponse getUser() {
        return user;
    }

    public JwsResponse setUser(UserResponse user) {
        this.user = user;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JwsResponse that = (JwsResponse) o;
        return Objects.equals(jws, that.jws) && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jws, user);
    }

    @Override
    public String toString() {
        return "JwsResponse{" +
                "jws='" + jws + '\'' +
                ", user=" + user +
                '}';
    }

}
