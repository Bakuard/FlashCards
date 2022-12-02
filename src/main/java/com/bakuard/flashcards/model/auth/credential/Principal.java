package com.bakuard.flashcards.model.auth.credential;

import com.bakuard.flashcards.model.auth.resource.Param;
import com.bakuard.flashcards.model.auth.resource.Params;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Principal {

    public static Principal of(UUID id) {
        return newBuilder().setId(id).build();
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    private final UUID id;
    private final Params params;

    private Principal(UUID id, Params params) {
        this.id = Objects.requireNonNull(id, "Principal id can't be null.");
        this.params = params;
    }

    public UUID getId() {
        return id;
    }

    public boolean hasDetail() {
        return params.hasParam("detail");
    }

    public boolean detailTypeIs(Class<?> type) {
        return params.findFirstByKey("detail").
                map(param -> param.isValueType(type)).
                orElse(false);
    }

    public <T> Optional<T> getDetailAs(Class<T> type) {
        return params.findFirstValueByKeyAs("detail", type);
    }

    public Params getParams() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Principal principal = (Principal) o;
        return Objects.equals(id, principal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Principal{" +
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

        public Builder setId(UUID id) {
            this.id = id == null ? UUID.randomUUID() : id;
            return this;
        }

        public Builder setDetail(Object detail) {
            params.addParam("detail", detail);
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

        public Principal build() {
            return new Principal(id, params.build());
        }

    }

}
