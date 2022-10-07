package com.bakuard.flashcards.model.auth.resource;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Resource {

    public static Resource of(Object payload) {
        return newBuilder().setPayload(payload).build();
    }

    public static Resource of(String type, Object payload) {
        return Resource.newBuilder().
                setType(type).
                setPayload(payload).
                build();
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    private final UUID id;
    private final Params params;

    private Resource(UUID id, Params params) {
        this.id = id;
        this.params = params;
    }

    public UUID getId() {
        return id;
    }

    public Optional<String> getType() {
        return params.findFirstValueByKeyAs("type", String.class);
    }

    public boolean typeIs(String type) {
        return params.findFirstValueByKeyAs("type", String.class).
                map(t -> t.equals(type)).
                orElse(false);
    }

    public boolean hasType() {
        return params.hasParam("type");
    }

    public boolean containsPayload() {
        return params.hasParam("payload");
    }

    public boolean payloadTypeIs(Class<?> type) {
        return params.findFirstByKey("payload").
                map(param -> param.isValueType(type)).
                orElse(false);
    }

    public <T> Optional<T> getPayloadAs(Class<T> type) {
        return params.findFirstValueByKeyAs("payload", type);
    }

    public List<Action> getActions() {
        return params.findAllValuesByKeyAs("action", Action.class);
    }

    public boolean hasAction(Action action) {
        return params.containsValueByKey("action", action);
    }

    public boolean hasAction(String action) {
        return hasAction(new Action(action));
    }

    public Params getParams() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(id, resource.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id=" + id +
                ", params=" + params +
                '}';
    }


    public static class Builder {

        private UUID id;
        private final Params.Builder params;

        private Builder() {
            this.id = UUID.randomUUID();
            this.params = Params.newBuilder();
        }

        public Builder setOrGenerateId(UUID id) {
            this.id = id == null ? UUID.randomUUID() : id;
            return this;
        }

        public Builder setType(String type) {
            params.replaceFirstParamOrAdd("type", type);
            return this;
        }

        public Builder setPayload(Object payload) {
            params.replaceFirstParamOrAdd("payload", payload);
            return this;
        }

        public Builder addAction(Action action) {
            params.addParam("action", action);
            return this;
        }

        public Builder addAction(String action) {
            params.addParam("action", new Action(action));
            return this;
        }

        public Builder addParam(Param param) {
            params.addParam(param);
            return this;
        }

        public Builder addParam(String key, Object value) {
            params.addParam(key, value);
            return this;
        }

        public Resource build() {
            return new Resource(id, params.build());
        }

    }

}
