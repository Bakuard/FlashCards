package com.bakuard.flashcards.model.auth.request;

import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.model.auth.policy.Access;
import com.bakuard.flashcards.model.auth.resource.Action;
import com.bakuard.flashcards.model.auth.resource.Resource;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

public class AuthRequest {

    public static AuthRequest of(User user, Resource resource, Action action) {
        return new AuthRequest(user, resource, action);
    }

    public static AuthRequest of(User user, Resource resource, String action) {
        return newBuilder().
                setUser(user).
                setResource(resource).
                setAction(action).
                build();
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    private final User user;
    private final Resource resource;
    private final Action action;

    private AuthRequest(User user, Resource resource, Action action) {
        this.user = user;
        this.resource = resource;
        this.action = action;
    }

    public Optional<User> getUser() {
        return Optional.ofNullable(user);
    }

    public Optional<Resource> getResource() {
        return Optional.ofNullable(resource);
    }

    public Optional<Action> getAction() {
        return Optional.ofNullable(action);
    }

    public boolean isUserAndResourcesPresent() {
        return user != null && resource != null;
    }

    public boolean isUserAndActionPresent() {
        return user != null && action != null;
    }

    public boolean isResourceAndActionPresent() {
        return resource != null && action != null;
    }

    public boolean isUserAndResourceAndActionPresent() {
        return user != null && resource != null && action != null;
    }

    public boolean isUserAndResourceAndActionEmpty() {
        return user == null && resource == null && action == null;
    }

    public Access mapUserAndResource(BiFunction<User, Resource, Access> mapper) {
        Access result = Access.UNKNOWN;
        if(isUserAndResourcesPresent()) result = mapper.apply(user, resource);
        return result;
    }

    public Access mapUserAndAction(BiFunction<User, Action, Access> mapper) {
        Access result = Access.UNKNOWN;
        if(isUserAndActionPresent()) result = mapper.apply(user, action);
        return result;
    }

    public Access mapResourceAndAction(BiFunction<Resource, Action, Access> mapper) {
        Access result = Access.UNKNOWN;
        if(isResourceAndActionPresent()) result = mapper.apply(resource, action);
        return result;
    }

    public Access mapUserAndResourceAndAction(TriFunction<User, Resource, Action, Access> mapper) {
        Access result = Access.UNKNOWN;
        if(isUserAndResourceAndActionPresent()) result = mapper.apply(user, resource, action);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthRequest that = (AuthRequest) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(resource, that.resource) &&
                Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, resource, action);
    }

    @Override
    public String toString() {
        return "AuthRequest{" +
                "user=" + user +
                ", resource=" + resource +
                ", action=" + action +
                '}';
    }


    public static class Builder {

        private User user;
        private Resource resource;
        private Action action;

        private Builder() {

        }

        public Builder setUser(User user) {
            this.user = user;
            return this;
        }

        public Builder setResource(Resource resource) {
            this.resource = resource;
            return this;
        }

        public Builder setAction(Action action) {
            this.action = action;
            return this;
        }

        public Builder setAction(String action) {
            this.action = action == null ? null : new Action(action);
            return this;
        }

        public AuthRequest build() {
            return new AuthRequest(user, resource, action);
        }

    }

}
