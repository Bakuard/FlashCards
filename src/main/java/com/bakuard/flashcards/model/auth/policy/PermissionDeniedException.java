package com.bakuard.flashcards.model.auth.policy;

import com.bakuard.flashcards.model.auth.credential.Principal;
import com.bakuard.flashcards.model.auth.request.AuthRequest;
import com.bakuard.flashcards.model.auth.resource.Action;
import com.bakuard.flashcards.model.auth.resource.Resource;

import java.util.Optional;

public class PermissionDeniedException extends RuntimeException {

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

    public Optional<Principal> getPrincipal() {
        return Optional.ofNullable(principal);
    }

    public Optional<Resource> getResource() {
        return Optional.ofNullable(resource);
    }

    public Optional<Action> getAction() {
        return Optional.ofNullable(action);
    }


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

        public Builder setUserAndResourceAndActionBy(AuthRequest request) {
            principal = request.getPrincipal().orElse(null);
            action = request.getAction().orElse(null);
            resource = request.getResource().orElse(null);
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

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
                            append(r.getId()).
                            append('}'),
                    () -> sb.append("null")
            );

            message = sb.toString();

            return this;
        }

        public Builder setCause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public Builder setEnableSuppression(boolean enableSuppression) {
            this.enableSuppression = enableSuppression;
            return this;
        }

        public Builder setWritableStackTrace(boolean writableStackTrace) {
            this.writableStackTrace = writableStackTrace;
            return this;
        }

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
