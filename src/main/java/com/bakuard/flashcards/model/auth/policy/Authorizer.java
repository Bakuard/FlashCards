package com.bakuard.flashcards.model.auth.policy;

import com.bakuard.flashcards.model.auth.credential.Principal;
import com.bakuard.flashcards.model.auth.request.AuthRequest;
import com.bakuard.flashcards.model.auth.resource.Action;
import com.bakuard.flashcards.model.auth.resource.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Проверяет доступ для запроса представленного в виде {@link AuthRequest}, последовательно перебирая цепочку
 * политик до тех пор, пока одна из политик не вернет {@link Access#ACCEPT} или {@link Access#DENY},
 * или не закончатся политики в цепочке.
 */
public class Authorizer {

    /**
     * Создается и возвращается новый объект для создания экземпляра {@link Authorizer}.
     * @return новый объект для создания экземпляра {@link Authorizer}.
     */
    public static Builder newBuilder() {
        return new Builder();
    }


    private final List<Policy> policies;
    private final boolean strictMode;

    private Authorizer(List<Policy> policies, boolean strictMode) {
        this.policies = policies;
        this.strictMode = strictMode;
    }

    public Access checkAccess(UUID principalId, String resourceType, Object resourcePayload, String action) {
        return checkAccess(Principal.of(principalId), Resource.of(resourceType, resourcePayload), action);
    }

    public Access checkAccess(Principal principal, Resource resource, String action) {
        return checkAccess(AuthRequest.of(principal, resource, action));
    }

    /**
     * Проверяет: <br/>
     * 1. имеет ли право пользователь {@link AuthRequest#getPrincipal()} <br/>
     * 2. выполнить действие {@link AuthRequest#getAction()} <br/>
     * 3. над ресурсом {@link AuthRequest#getResource()} <br/>
     * Результат будет представлять один из трех вариантов описанных в {@link Access}.
     * @param request см. {@link AuthRequest}
     * @return результат проверки в виде одного из трех вариантов описанных в {@link Access}.
     */
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

    /**
     * Проверяет: <br/>
     * 1. имеет ли право пользователь {@link AuthRequest#getPrincipal()} <br/>
     * 2. выполнить действие {@link AuthRequest#getAction()} <br/>
     * 3. над ресурсом {@link AuthRequest#getResource()} <br/>
     *
     * @param request см. {@link AuthRequest}
     */
    public void assertToHasAccess(AuthRequest request) {
        Access access = checkAccess(request);
        if(access == Access.DENY || access == Access.UNKNOWN && strictMode) {
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
        private boolean strictMode;

        private Builder() {
            policies = new ArrayList<>();
            strictMode = true;
        }

        public Builder policy(Policy policy) {
            policies.add(policy);
            return this;
        }

        public Builder throwIfAccessCheckReturnUnknown(boolean strictMode) {
            this.strictMode = strictMode;
            return this;
        }

        public Authorizer build() {
            return new Authorizer(policies, strictMode);
        }

    }

}
