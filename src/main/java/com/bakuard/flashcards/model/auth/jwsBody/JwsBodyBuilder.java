package com.bakuard.flashcards.model.auth.jwsBody;

import com.bakuard.flashcards.model.auth.resource.Action;
import com.bakuard.flashcards.model.auth.resource.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class JwsBodyBuilder {

    public static JwsBodyBuilder newBuilder() {
        return new JwsBodyBuilder();
    }


    private UUID tokenId;
    private UUID userID;
    private List<Permission> permissions;

    private JwsBodyBuilder() {
        tokenId = UUID.randomUUID();
        permissions = new ArrayList<>();
    }

    public JwsBodyBuilder setOrGenerateTokenId(UUID tokenId) {
        this.tokenId = tokenId == null ? UUID.randomUUID() : tokenId;
        return this;
    }

    public JwsBodyBuilder setUserID(UUID userID) {
        this.userID = userID;
        return this;
    }

    public JwsBodyBuilder addPair(Permission permission) {
        permissions.add(permission);
        return this;
    }

    public PermissionBuilder newPermissionBuilder() {
        return new PermissionBuilder(this);
    }

    public JwsBody build() {
        return new JwsBody(tokenId, userID, permissions);
    }


    public static class PermissionBuilder {

        private JwsBodyBuilder jwsBodyBuilder;
        private UUID resourceId;
        private List<String> actions;

        private PermissionBuilder(JwsBodyBuilder jwsBodyBuilder) {
            this.jwsBodyBuilder = jwsBodyBuilder;
            this.actions = new ArrayList<>();
        }

        public PermissionBuilder setResourceId(UUID resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public PermissionBuilder setResourceId(Resource resource) {
            resourceId = resource.getId();
            return this;
        }

        public PermissionBuilder addAction(String action) {
            actions.add(action);
            return this;
        }

        public PermissionBuilder addAction(Action action) {
            actions.add(action.name());
            return this;
        }

        public PermissionBuilder setActions(List<Action> actions) {
            this.actions = actions.stream().map(Action::name).collect(Collectors.toCollection(ArrayList::new));
            return this;
        }

        public JwsBodyBuilder buildPair() {
            return jwsBodyBuilder.addPair(new Permission(resourceId, actions));
        }

    }

}
