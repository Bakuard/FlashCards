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
 * или не закончатся политики в цепочке. <br/><br/>
 * Объекты данного класса не изменяемы.
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
        this.policies = new ArrayList<>(policies);
        this.strictMode = strictMode;
    }

    /**
     * Проверяет: <br/>
     * 1. имеет ли право пользователь с идентификатором - principalId <br/>
     * 2. выполнить действие - action <br/>
     * 3. над ресурсом представленным как resourceType и resourcePayload <br/><br/>
     * Результат будет представлен в виде одного из трех вариантов описанных в {@link Access}.
     * @param principalId идентификатор пользователя делающего запрос.
     * @param resourceType тип ресурса (подробнее см. {@link Resource}).
     * @param resourcePayload полезные данные ресурса (подробнее см. {@link Resource}).
     * @param action действие, которое собираются выполнить над ресурсом.
     */
    public Access checkAccess(UUID principalId, String resourceType, Object resourcePayload, String action) {
        return checkAccess(Principal.of(principalId), Resource.of(resourceType, resourcePayload), action);
    }

    /**
     * Проверяет: <br/>
     * 1. имеет ли право пользователь - principal <br/>
     * 2. выполнить действие - action <br/>
     * 3. над ресурсом - resource <br/><br/>
     * Результат будет представлен в виде одного из трех вариантов описанных в {@link Access}.
     * @param principal пользователь делающий запрос
     * @param resource ресурс, к которому запрашивается доступ
     * @param action дейтсвие, которое собираются выполнить над ресурсом
     */
    public Access checkAccess(Principal principal, Resource resource, String action) {
        return checkAccess(AuthRequest.of(principal, resource, action));
    }

    /**
     * Проверяет: <br/>
     * 1. имеет ли право пользователь - {@link AuthRequest#getPrincipal()} <br/>
     * 2. выполнить действие - {@link AuthRequest#getAction()} <br/>
     * 3. над ресурсом - {@link AuthRequest#getResource()} <br/>
     * Результат будет будет представлен в виде одного из трех вариантов описанных в {@link Access}.
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

    /**
     * Проверяет: <br/>
     * 1. имеет ли право пользователь с идентификатором - principalId <br/>
     * 2. выполнить действие - action <br/>
     * 3. над ресурсом представленным как resourceType и resourcePayload <br/><br/>
     * Если результат проверки({@link #checkAccess(AuthRequest)}) соответствует одному из двух вариантов:<br/>
     * 1. {@link Access#DENY} <br/>
     * 2. {@link Access#UNKNOWN} и при создании текущего объекта Authorizer был вызван метод
     *    {@link Builder#throwIfAccessCheckReturnUnknown(boolean)} c аргументом true <br/><br/>
     * то будет выбрашенно исключение. Иначе метод ничего не делает.
     * @param principalId идентификатор пользователя делающего запрос
     * @param resourceType тип ресурса (подробнее см. {@link Resource})
     * @param resourcePayload полезные данные ресурса (подробнее см. {@link Resource})
     * @param action дейтсвие, которое собираются выполнить над ресурсом
     * @throws PermissionDeniedException если доступ для переданных principalId, action, resourceType
     *                                   и resourcePayload отклонен.
     */
    public void assertToHasAccess(UUID principalId, String resourceType, Object resourcePayload, String action) {
        assertToHasAccess(Principal.of(principalId), Resource.of(resourceType, resourcePayload), action);
    }

    /**
     * Проверяет: <br/>
     * 1. имеет ли право пользователь - principal <br/>
     * 2. выполнить действие - action <br/>
     * 3. над ресурсом - resource <br/><br/>
     * Если результат проверки({@link #checkAccess(AuthRequest)}) соответствует одному из двух вариантов:<br/>
     * 1. {@link Access#DENY} <br/>
     * 2. {@link Access#UNKNOWN} и при создании текущего объекта Authorizer был вызван метод
     *    {@link Builder#throwIfAccessCheckReturnUnknown(boolean)} c аргументом true <br/><br/>
     * то будет выбрашенно исключение. Иначе метод ничего не делает.
     * @param principal пользователь делающий запрос
     * @param resource ресурс, к которому запрашивается доступ
     * @param action дейтсвие, которое собираются выполнить над ресурсом
     * @throws PermissionDeniedException если доступ для переданных principal, action и resource отклонен.
     */
    public void assertToHasAccess(Principal principal, Resource resource, String action) {
        assertToHasAccess(AuthRequest.of(principal, resource, action));
    }

    /**
     * Проверяет: <br/>
     * 1. имеет ли право пользователь - {@link AuthRequest#getPrincipal()} <br/>
     * 2. выполнить действие - {@link AuthRequest#getAction()} <br/>
     * 3. над ресурсом - {@link AuthRequest#getResource()} <br/><br/>
     * Если результат проверки({@link #checkAccess(AuthRequest)}) соответствует одному из двух вариантов:<br/>
     * 1. {@link Access#DENY} <br/>
     * 2. {@link Access#UNKNOWN} и при создании текущего объекта Authorizer был вызван метод
     *    {@link Builder#throwIfAccessCheckReturnUnknown(boolean)} c аргументом true <br/><br/>
     * то будет выбрашенно исключение. Иначе метод ничего не делает.
     * @param request см. {@link AuthRequest}
     * @throws PermissionDeniedException если доступ для переданного {@link AuthRequest} отклонен.
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

    /**
     * Из всех действий, которые можно выполнить над ресурсом (см. {@link Resource#getActions()}),
     * возвращает те, которые доступны для principal.
     * @param principal пользователь, для которого подбирается список доступных ему действий над ресурсом resource.
     * @param resource ресурс из действий которого отбираются те, что доступны пользователю principal.
     * @return список действий доступных для пользователя principal над ресурсом resource.
     */
    public List<Action> getAccessibleActions(Principal principal, Resource resource) {
        return resource.getActions().stream().
                filter(action -> checkAccess(AuthRequest.of(principal, resource, action)) == Access.ACCEPT).
                toList();
    }

    /**
     * Из всех действий, которые можно выполнить над ресурсом (см. {@link Resource#getActions()}),
     * возвращает те, которые доступны для principal. Если нет ни одного действия доступного пользователю
     * над указанным ресурсом - выбрасывает исключение.
     * @param principal пользователь, для которого подбирается список доступных ему действий над ресурсом resource.
     * @param resource ресурс из действий которого отбираются те, что доступны пользователю principal.
     * @return список действий доступных для пользователя principal над ресурсом resource.
     * @throws PermissionDeniedException если нет ни одного действия доступного для указанного пользователя
     *                                   над указанным ресурсом.
     */
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


    /**
     * Реализация паттерна Builder для создания объектов {@link Authorizer}. Один и тот же объект
     * может использоваться для создания разных и независмых(с точки зрения разделяемых данных) объектов
     * указанного типа.
     */
    public static class Builder {

        private final List<Policy> policies;
        private boolean strictMode;

        private Builder() {
            policies = new ArrayList<>();
            strictMode = true;
        }

        /**
         * Добавляет указанную политику в цепочку политик.
         * @param policy добавляемая политика.
         * @return ссылку на этот же объект.
         */
        public Builder policy(Policy policy) {
            policies.add(policy);
            return this;
        }

        /**
         * Указывает - разрешать ли доступ, если цепочка политик(см. {@link Authorizer}) вернула
         * {@link Access#UNKNOWN}.
         * @param strictMode true - запрещает доступ, если цепочка политик вернула {@link Access#UNKNOWN},
         *                   false - разрешает доступ в описанном выше случае.
         * @return ссылку на этот же объект.
         */
        public Builder throwIfAccessCheckReturnUnknown(boolean strictMode) {
            this.strictMode = strictMode;
            return this;
        }

        /**
         * Создает и возвращает новй объект {@link Authorizer}.
         * @return овй объект {@link Authorizer}.
         */
        public Authorizer build() {
            return new Authorizer(policies, strictMode);
        }

    }

}
