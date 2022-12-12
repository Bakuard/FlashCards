package com.bakuard.flashcards.model.auth.policy;

import com.bakuard.flashcards.model.auth.credential.Principal;
import com.bakuard.flashcards.model.auth.request.AuthRequest;
import com.bakuard.flashcards.model.auth.resource.Action;
import com.bakuard.flashcards.model.auth.resource.Resource;

import java.util.Optional;

/**
 * Выбрасывается, если отказано в доступе при выполнении определенного запроса.
 */
public class PermissionDeniedException extends RuntimeException {

    /**
     * Создает и возвращает объект используемый для создания экземпляра PermissionDeniedException со всеми
     * необходимыми данными.
     * @return объект для создания экземпляра PermissionDeniedException.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    private final Principal principal;
    private final Resource resource;
    private final Action action;

    private PermissionDeniedException(String message,
                                      Throwable cause,
                                      boolean enableSuppression,
                                      boolean writableStackTrace,
                                      Principal principal,
                                      Resource resource,
                                      Action action) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.principal = principal;
        this.resource = resource;
        this.action = action;
    }

    /**
     * Возвращает пользователя, которому было отказано в доступе.
     * @return пользователь, которому было отказано в доступе.
     */
    public Optional<Principal> getPrincipal() {
        return Optional.ofNullable(principal);
    }

    /**
     * Возвращает ресурс, к которому пользователь не смог получить доступ.
     * @return ресурс, к которому пользователь не смог получить доступ.
     */
    public Optional<Resource> getResource() {
        return Optional.ofNullable(resource);
    }

    /**
     * Возвращает описание действия над ресурсом, в выполнении которого было отказано пользователю.
     * @return описание действия над ресурсом, в выполнении которого было отказано пользователю.
     */
    public Optional<Action> getAction() {
        return Optional.ofNullable(action);
    }


    /**
     * Реализация паттерна Builder для создания объектов {@link PermissionDeniedException}. Один и тот же объект
     * может использоваться для создания разных и независимых(с точки зрения разделяемых данных) объектов исключений.
     */
    public static class Builder {

        private Principal principal;
        private Resource resource;
        private Action action;
        private String message;
        private Throwable cause;
        private boolean enableSuppression;
        private boolean writableStackTrace;

        private Builder() {
            enableSuppression = true;
            writableStackTrace = true;
        }

        /**
         * Устанавливает пользователя, к которому отказано в доступе при выполнении определенного запроса.
         * @param principal пользователь, к которому отказано в доступе при выполнении определенного запроса.
         * @return ссылку на этот же объект.
         */
        public Builder setPrincipal(Principal principal) {
            this.principal = principal;
            return this;
        }

        /**
         * Устанавливает ресурс, в доступе к которому отказано.
         * @param resource ресурс, в доступе к которому отказано.
         * @return ссылку на этот же объект.
         */
        public Builder setResource(Resource resource) {
            this.resource = resource;
            return this;
        }

        /**
         * Устанавливает действие над ресурсом, в выполнении которого было отказано из-за нехватки прав доступа.
         * @param action действие над ресурсом, в выполнении которого было отказано из-за нехватки прав доступа.
         * @return ссылку на этот же объект.
         */
        public Builder setAction(Action action) {
            this.action = action;
            return this;
        }

        /**
         * Устанавливает: <br/>
         * 1. действие над ресурсом, в выполнении которого было отказано из-за нехватки прав доступа. <br/>
         * 2. ресурс, в доступе к которому отказано. <br/>
         * 3. пользователя, к которому отказано в доступе при выполнении определенного запроса. <br/>
         * на основе параметров запроса.
         * @param request (см {@link AuthRequest}).
         * @return ссылку на этот же объект.
         */
        public Builder setUserAndResourceAndActionBy(AuthRequest request) {
            principal = request.getPrincipal().orElse(null);
            action = request.getAction().orElse(null);
            resource = request.getResource().orElse(null);
            return this;
        }

        /**
         * @see <a href="https://docs.oracle.com/en/java/javase/18/docs/api/java.base/java/lang/Throwable.html#%3Cinit%3E(java.lang.String)">документация к Throwable</a>
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * Устанавливает сообщение об ошибке сгенерированное на основе параметров запроса.
         * @param request (см {@link AuthRequest}).
         * @return ссылку на этот же объект.
         */
        public Builder setMessageBy(AuthRequest request) {
            StringBuilder sb = new StringBuilder("Permission denied for: ");

            sb.append("principal=");
            request.getPrincipal().ifPresentOrElse(
                    p -> sb.append(p.getId()), () -> sb.append("null")
            );
            sb.append(", action=");
            request.getAction().ifPresentOrElse(
                    a -> sb.append(a.name()), () -> sb.append("null")
            );
            sb.append(", resource=");
            request.getResource().ifPresentOrElse(
                    r -> sb.append('{').
                            append(r.getType()).
                            append('|').
                            append(r.getPayloadAs(Object.class)).
                            append('}'),
                    () -> sb.append("null")
            );

            message = sb.toString();

            return this;
        }

        /**
         * @see <a href="https://docs.oracle.com/en/java/javase/18/docs/api/java.base/java/lang/Throwable.html#initCause(java.lang.Throwable)">документация к Throwable</a>
         */
        public Builder setCause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        /**
         * @see <a href="https://docs.oracle.com/en/java/javase/18/docs/api/java.base/java/lang/Throwable.html#addSuppressed(java.lang.Throwable)">документация к Throwable</a>
         */
        public Builder setEnableSuppression(boolean enableSuppression) {
            this.enableSuppression = enableSuppression;
            return this;
        }

        /**
         * @see <a href="https://docs.oracle.com/en/java/javase/18/docs/api/java.base/java/lang/Throwable.html#%3Cinit%3E(java.lang.String,java.lang.Throwable,boolean,boolean)">документация к Throwable</a>
         */
        public Builder setWritableStackTrace(boolean writableStackTrace) {
            this.writableStackTrace = writableStackTrace;
            return this;
        }

        /**
         * Создает и возвращает новый объект исключения {@link PermissionDeniedException}
         * @return новый объект исключения {@link PermissionDeniedException}
         */
        public PermissionDeniedException build() {
            return new PermissionDeniedException(
                    message,
                    cause,
                    enableSuppression,
                    writableStackTrace,
                    principal,
                    resource,
                    action
            );
        }

    }

}
