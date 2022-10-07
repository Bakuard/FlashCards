package com.bakuard.flashcards.model.auth.policy;

import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.model.auth.request.AuthRequest;
import com.bakuard.flashcards.model.auth.resource.Action;
import com.bakuard.flashcards.model.auth.resource.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Authorizer {

    public static Builder newBuilder() {
        return new Builder();
    }


    private final List<Policy> policies;
    private final Access exceptionLevel;

    private Authorizer(List<Policy> policies, Access exceptionLevel) {
        this.policies = policies;
        this.exceptionLevel = exceptionLevel;
    }

    public Access checkAccess(User user, Object payload, String action) {
        return checkAccess(AuthRequest.of(user, Resource.of(payload), action));
    }

    public Access checkAccess(AuthRequest request) {
        Access result = Access.UNKNOWN;
        int i = 0;
        for(int size = policies.size(); i < size && result == Access.UNKNOWN; ++i) {
            result = policies.get(i).checkAccess(request);
        }
        return Objects.requireNonNull(result,
                "Policy can't return null. Policy with index=" + i + " violated this rule.");
    }

    public void assertThatCanAccess(AuthRequest request) {
        if(checkAccess(request).getLevel() <= exceptionLevel.getLevel()) {
            throw PermissionDeniedException.newBuilder().
                    setUserAndResourceAndActionBy(request).
                    setMessageBy(request).
                    build();
        }
    }

    public void assertThatCanAccess(User user, Object payload, String action) {
        assertThatCanAccess(AuthRequest.of(user, Resource.of(payload), action));
    }

    public List<Action> getAccessibleActions(User user, Resource resource) {
        return resource.getActions().stream().
                filter(action -> checkAccess(AuthRequest.of(user, resource, action)) == Access.ACCEPT).
                toList();
    }

    public List<Action> tryGetAccessibleActions(User user, Resource resource) {
        List<Action> result = getAccessibleActions(user, resource);
        if(result.isEmpty()) {
            throw PermissionDeniedException.newBuilder().
                    setUser(user).
                    setResource(resource).
                    setMessageBy(AuthRequest.of(user, resource, (Action)null)).
                    build();
        }
        return result;
    }


    public static class Builder {

        private List<Policy> policies;
        private Access exceptionLevel;

        private Builder() {
            policies = new ArrayList<>();
            exceptionLevel = Access.UNKNOWN;
        }

        public Builder policy(Policy policy) {
            policies.add(policy);
            return this;
        }

        public Builder throwIfResultLessOrEqual(Access exceptionLevel) {
            this.exceptionLevel = exceptionLevel;
            return this;
        }

        public Authorizer build() {
            return new Authorizer(policies, exceptionLevel);
        }

    }

}
