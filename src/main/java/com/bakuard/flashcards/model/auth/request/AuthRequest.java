package com.bakuard.flashcards.model.auth.request;

import com.bakuard.flashcards.model.auth.credential.Principal;
import com.bakuard.flashcards.model.auth.policy.Access;
import com.bakuard.flashcards.model.auth.resource.Action;
import com.bakuard.flashcards.model.auth.resource.Resource;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Данные запроса, который необходимо проверить на наличие прав доступа, а именно:<br/>
 * 1. Какой пользователь выполняет запрос. <br/>
 * 2. К какому ресурсу выполняется запрос. <br/>
 * 3. Какое действие пользователь собирается выполнить над ресурсом. <br/><br/>
 * Объекты данного класса являются не изменяемыми.
 */
public class AuthRequest {

    /**
     * Создает и возвращает набор данных, необходимых для проверки прав доступа некоторого запроса.
     * Любой из параметров может принимать значение null.
     * @param principal пользователь делающий запрос.
     * @param resource ресурс, к которому делается запрос.
     * @param action действие, которое пользователь собирается выполнить над указанным ресурсом.
     * @return объект содержащий все необходимые данные для проверки прав доступа.
     */
    public static AuthRequest of(Principal principal, Resource resource, Action action) {
        return new AuthRequest(principal, resource, action);
    }

    /**
     * Создает и возвращает набор данных, необходимых для проверки прав доступа некоторого запроса.
     * Любой из параметров может принимать значение null.
     * @param principal пользователь делающий запрос.
     * @param resource ресурс, к которому делается запрос.
     * @param action действие, которое пользователь собирается выполнить над указанным ресурсом.
     * @return объект содержащий все необходимые данные для проверки прав доступа.
     */
    public static AuthRequest of(Principal principal, Resource resource, String action) {
        return newBuilder().
                setPrincipal(principal).
                setResource(resource).
                setAction(action).
                build();
    }

    /**
     * Создает и возвращает объект используемый для создания экземпляра AuthRequest со всеми необходимыми данными.
     * @return объект используемый для создания экземпляра AuthRequest.
     */
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

    /**
     * Возвращает данные пользователя делающего запрос.
     * @return данные пользователя делающего запрос.
     */
    public Optional<Principal> getPrincipal() {
        return Optional.ofNullable(principal);
    }

    /**
     * Возвращает ресурс, к которому делается запрос.
     * @return ресурс, к которому делается запрос.
     */
    public Optional<Resource> getResource() {
        return Optional.ofNullable(resource);
    }

    /**
     * Возвращает описание действия, которое пользователь собирается выполнить над ресурсом.
     * @return действие, которое пользователь собирается выполнить над ресурсом.
     */
    public Optional<Action> getAction() {
        return Optional.ofNullable(action);
    }

    /**
     * Проверяет - заданны ли пользователь или ресурс.
     * @return true - если условие выше выполняется, иначе - false.
     */
    public boolean hasPrincipalAndResources() {
        return principal != null && resource != null;
    }

    /**
     * Проверяет - заданны ли пользователь или действие над ресурсом.
     * @return true - если условие выше выполняется, иначе - false.
     */
    public boolean hasPrincipalAndAction() {
        return principal != null && action != null;
    }

    /**
     * Проверяет - заданны ли ресурс и выполняемое над ним действие.
     * @return true - если условие выше выполняется, иначе - false.
     */
    public boolean hasResourceAndAction() {
        return resource != null && action != null;
    }

    /**
     * Проверяет - заданны ли пользователь, ресурс и выполняемое над ним действие.
     * @return true - если условие выше выполняется, иначе - false.
     */
    public boolean hasPrincipalAndResourceAndAction() {
        return principal != null && resource != null && action != null;
    }

    /**
     * Проверяет, что для пользователя, ресурса и действия над ресурсом - не задано никаких значений.
     * @return true - если условие выше выполняется, иначе - false.
     */
    public boolean hasNotPrincipalAndResourceAndAction() {
        return principal == null && resource == null && action == null;
    }

    /**
     * Если для пользователя и ресурса заданы значения, то управление передается функции mapper, которая на
     * основе этих данных выполняет проверку прав доступа. Если для пользователя или ресурса не заданы
     * значения - метод возвращает {@link Access#UNKNOWN}.
     * @param mapper функция принимающая пользователя и ресурс, и возвращающая результат проверки
     *               прав доступа в виде перечисления {@link Access}.
     * @return результат проверки прав доступа функцией mapper или {@link Access#UNKNOWN}.
     */
    public Access mapPrincipalAndResource(BiFunction<Principal, Resource, Access> mapper) {
        Access result = Access.UNKNOWN;
        if(hasPrincipalAndResources()) result = mapper.apply(principal, resource);
        return result;
    }

    /**
     * Если для пользователя и действия над ресурсом заданы значения, то управление передается функции mapper,
     * которая на основе этих данных выполняет проверку прав доступа. Если для пользователя или действия
     * над ресурсом не заданы значения - метод возвращает {@link Access#UNKNOWN}.
     * @param mapper функция принимающая пользователя и действие над ресурсом, и возвращающая результат проверки
     *               прав доступа в виде перечисления {@link Access}.
     * @return результат проверки прав доступа функцией mapper или {@link Access#UNKNOWN}.
     */
    public Access mapPrincipalAndAction(BiFunction<Principal, Action, Access> mapper) {
        Access result = Access.UNKNOWN;
        if(hasPrincipalAndAction()) result = mapper.apply(principal, action);
        return result;
    }

    /**
     * Если для ресурса и действия над ресурсом заданы значения, то управление передается функции mapper,
     * которая на основе этих данных выполняет проверку прав доступа. Если для ресурса или действия
     * над ресурсом не заданы значения - метод возвращает {@link Access#UNKNOWN}.
     * @param mapper функция принимающая ресурс и действие над ресурсом, и возвращающая результат проверки
     *               прав доступа в виде перечисления {@link Access}.
     * @return результат проверки прав доступа функцией mapper или {@link Access#UNKNOWN}.
     */
    public Access mapResourceAndAction(BiFunction<Resource, Action, Access> mapper) {
        Access result = Access.UNKNOWN;
        if(hasResourceAndAction()) result = mapper.apply(resource, action);
        return result;
    }

    /**
     * Если для пользователя, ресурса и действия над ресурсом заданы значения, то управление передается функции
     * mapper, которая на основе этих данных выполняет проверку прав доступа. Если для пользователя, ресурса или
     * действия над ресурсом не заданы значения - метод возвращает {@link Access#UNKNOWN}.
     * @param mapper функция принимающая пользователя, ресурс и действия над ресурсом, и возвращающая результат
     *               проверки прав доступа в виде перечисления {@link Access}.
     * @return результат проверки прав доступа функцией mapper или {@link Access#UNKNOWN}.
     */
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


    /**
     * Реализация паттерна Builder для создания объектов {@link AuthRequest}. Один и тот же объект
     * может использоваться для создания разных и независимых(с точки зрения разделяемых данных) объектов исключений.
     */
    public static class Builder {

        private Principal principal;
        private Resource resource;
        private Action action;

        private Builder() {

        }

        /**
         * Задает пользователя выполняющего запрос, который требует проверки прав доступа.
         * @param principal пользователь выполняющий запрос, который требует проверки прав доступа.
         * @return ссылку на это же объект.
         */
        public Builder setPrincipal(Principal principal) {
            this.principal = principal;
            return this;
        }

        /**
         * Устанавливает ресурс, к которому выполняется запрос требующий проверки прав доступа.
         * @param resource ресурс, к которому выполняется запрос требующий проверки прав доступа.
         * @return ссылку на этот же объект.
         */
        public Builder setResource(Resource resource) {
            this.resource = resource;
            return this;
        }

        /**
         * Устанавливает описание действия, которое пользователь собирается выполнить над ресурсом.
         * @param action описание действия, которое пользователь собирается выполнить над ресурсом.
         * @return ссылку на этот же объект.
         */
        public Builder setAction(Action action) {
            this.action = action;
            return this;
        }

        /**
         * Устанавливает описание действия, которое пользователь собирается выполнить над ресурсом.
         * @param action описание действия, которое пользователь собирается выполнить над ресурсом.
         * @return ссылку на этот же объект.
         */
        public Builder setAction(String action) {
            this.action = action == null ? null : new Action(action);
            return this;
        }

        /**
         * Создает и возвращает новый объект {@link AuthRequest}.
         * @return новый объект {@link AuthRequest}.
         */
        public AuthRequest build() {
            return new AuthRequest(principal, resource, action);
        }

    }

}
