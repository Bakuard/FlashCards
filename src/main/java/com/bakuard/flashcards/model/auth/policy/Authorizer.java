package com.bakuard.flashcards.model.auth.policy;

import com.bakuard.flashcards.model.auth.credential.Principal;
import com.bakuard.flashcards.model.auth.request.AuthRequest;
import com.bakuard.flashcards.model.auth.resource.Action;
import com.bakuard.flashcards.model.auth.resource.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

    public Access checkAccess(UUID principalId, String resourceType, Object resourcePayload, String action) {
        return checkAccess(Principal.of(principalId), Resource.of(resourceType, resourcePayload), action);
    }

    public Access checkAccess(Principal principal, Resource resource, String action) {
        return checkAccess(AuthRequest.of(principal, resource, action));
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

    public void assertToHasAccess(UUID principalId, String resourceType, Object resourcePayload, String action) {
        assertToHasAccess(Principal.of(principalId), Resource.of(resourceType, resourcePayload), action);
    }

    public void assertToHasAccess(Principal principal, Resource resource, String action) {
        assertToHasAccess(AuthRequest.of(principal, resource, action));
    }

    public void assertToHasAccess(AuthRequest request) {
        if(checkAccess(request).getLevel() <= exceptionLevel.getLevel()) {
            throw PermissionDeniedException.newBuilder().
                    setUserAndResourceAndActionBy(request).
                    setMessageBy(request).
                    build();
        }
    }

    public List<Action> getAccessibleActions(Principal principal, Resource resource) {
        return resource.getActions().stream().
                filter(action -> checkAccess(AuthRequest.of(principal, resource, action)) == Access.ACCEPT).
                toList();
    }

    public List<Action> tryGetAccessibleActions(Principal principal, Resource resource) {
        List<Action> result = getAccessibleActions(principal, resource);
        if(result.isEmpty()) {
            throw PermissionDeniedException.newBuilder().
                    setPrincipal(principal).
                    setResource(resource).
                    setMessageBy(AuthRequest.of(principal, resource, (Action)null)).
                    build();
        }
        return result;
    }


    public static class Builder {

        private final List<Policy> policies;
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
