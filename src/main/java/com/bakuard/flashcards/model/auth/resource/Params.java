package com.bakuard.flashcards.model.auth.resource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Params {

    public static Builder newBuilder() {
        return new Builder();
    }


    private final List<Param> params;

    private Params(List<Param> params) {
        if(params.stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("Params can't contains null");
        if(params.stream().anyMatch(param -> param.key() == null || param.key().isBlank()))
            throw new IllegalArgumentException("Params key can't be blank or null");

        this.params = params;
    }

    public List<Param> getParams() {
        return Collections.unmodifiableList(params);
    }

    public boolean hasParam(String key) {
        return params.stream().anyMatch(param -> param.key().equals(key));
    }

    public boolean hasAllParams(String... keys) {
        Set<String> keysSet = params.stream().map(Param::key).collect(Collectors.toSet());
        return Arrays.stream(keys).allMatch(keysSet::contains);
    }

    public Optional<Object> findFirstValueByKey(String key) {
        return findFirstByKey(key).map(Param::value);
    }

    public <T> Optional<T> findFirstValueByKeyAs(String key, Class<T> type) {
        return findFirstByKey(key).map(param -> param.valueAs(type));
    }

    public Optional<Param> findFirstByKey(String key) {
        return params.stream().
                filter(param -> param.key().equals(key)).
                findFirst();
    }

    public List<Object> findAllValuesByKey(String key) {
        return params.stream().
                filter(param -> param.key().equals(key)).
                map(Param::value).
                collect(Collectors.toCollection(ArrayList::new));
    }

    public <T> List<T> findAllValuesByKeyAs(String key, Class<T> type) {
        return params.stream().
                filter(param -> param.key().equals(key)).
                map(param -> param.valueAs(type)).
                collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Param> findAllByKey(String key) {
        return params.stream().
                filter(param -> param.key().equals(key)).
                collect(Collectors.toCollection(ArrayList::new));
    }

    public boolean containsValueByKey(String key, Object value) {
        return params.stream().
                anyMatch(param -> param.key().equals(key) && Objects.equals(param.value(), value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Params params1 = (Params) o;
        return Objects.equals(params, params1.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(params);
    }

    @Override
    public String toString() {
        return "Params{" + params + '}';
    }


    public static class Builder {

        private final List<Param> params;

        private Builder() {
            this.params = new ArrayList<>();
        }

        public Builder setParams(List<Param> params) {
            if(params != null) {
                this.params.clear();
                this.params.addAll(params);
            }
            return this;
        }

        public Builder addParam(Param param) {
            params.add(param);
            return this;
        }

        public Builder addParam(String key, Object value) {
            params.add(new Param(key, value));
            return this;
        }

        public Builder replaceFirstParamOrAdd(Param param) {
            int index = IntStream.range(0, params.size()).
                    filter(i -> Objects.equals(params.get(i).key(), param.key())).
                    findFirst().
                    orElse(-1);
            if(index == -1) params.add(param);
            else params.set(index, param);
            return this;
        }

        public Builder replaceFirstParamOrAdd(String key, Object value) {
            return replaceFirstParamOrAdd(new Param(key, value));
        }

        public Params build() {
            return new Params(params);
        }

    }

}
