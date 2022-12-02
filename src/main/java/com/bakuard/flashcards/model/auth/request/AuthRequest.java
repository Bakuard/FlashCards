package com.bakuard.flashcards.model.auth.request;

import com.bakuard.flashcards.model.auth.credential.Principal;
import com.bakuard.flashcards.model.auth.policy.Access;
import com.bakuard.flashcards.model.auth.resource.Action;
import com.bakuard.flashcards.model.auth.resource.Resource;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

public class AuthRequest {

    public static AuthRequest of(Principal principal, Resource resource, Action action) {
        return new AuthRequest(principal, resource, action);
    }

    public static AuthRequest of(Principal principal, Resource resource, String action) {
        return newBuilder().
                setPrincipal(principal).
                setResource(resource).
                setAction(action).
                build();
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    private final Principal principal;
    private final Resource resource;
    private final Action action;

    private AuthRequest(Principal principal, Resource resource, Action action) {
        this.principal = principal;
        this.resource = resource;
        this.action = action;
    }

    public Optional<Principal> getPrincipal() {
        return Optional.ofNullable(principal);
    }

    public Optional<Resource> getResource() {
        return Optional.ofNullable(resource);
    }

    public Optional<Action> getAction() {
        return Optional.ofNullable(action);
    }

    public boolean hasPrincipalAndResources() {
        return principal != null && resource != null;
    }

    public boolean hasPrincipalAndAction() {
        return principal != null && action != null;
    }

    public boolean hasResourceAndAction() {
        return resource != null && action != null;
    }

    public boolean hasPrincipalAndResourceAndAction() {
        return principal != null && resource != null && action != null;
    }

    public boolean hasNotPrincipalAndResourceAndAction() {
        return principal == null && resource == null && action == null;
    }

    public Access mapPrincipalAndResource(BiFunction<Principal, Resource, Access> mapper) {
        Access result = Access.UNKNOWN;
        if(hasPrincipalAndResources()) result = mapper.apply(principal, resource);
        return result;
    }

    public Access mapPrincipalAndAction(BiFunction<Principal, Action, Access> mapper) {
        Access result = Access.UNKNOWN;
        if(hasPrincipalAndAction()) result = mapper.apply(principal, action);
        return result;
    }

    public Access mapResourceAndAction(BiFunction<Resource, Action, Access> mapper) {
        Access result = Access.UNKNOWN;
        if(hasResourceAndAction()) result = mapper.apply(resource, action);
        return result;
    }

    public Access mapPrincipalAndResourceAndAction(TriFunction<Principal, Resource, Action, Access> mapper) {
        Access result = Access.UNKNOWN;
        if(hasPrincipalAndResourceAndAction()) result = mapper.apply(principal, resource, action);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthRequest that = (AuthRequest) o;
        return Objects.equals(principal, that.principal) &&
                Objects.equals(resource, that.resource) &&
                Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principal, resource, action);
    }

    @Override
    public String toString() {
        return "AuthRequest{" +
                "principal=" + principal +
                ", resource=" + resource +
                ", action=" + action +
                '}';
    }


    public static class Builder {

        private Principal principal;
        private Resource resource;
        private Action action;

        private Builder() {

        }

        public Builder setPrincipal(Principal principal) {
            this.principal = principal;
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
            return new AuthRequest(principal, resource, action);
        }

    }

}
