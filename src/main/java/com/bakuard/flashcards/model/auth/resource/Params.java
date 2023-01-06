package com.bakuard.flashcards.model.auth.resource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Список свойств ({@link Param}) ресурса({@link Resource})
 * или пользователя({@link com.bakuard.flashcards.model.auth.credential.Principal}) для которых требуется
 * организовать разграничение доступа. <br/><br/>
 * <b>ВАЖНОЕ ОТЛИЧИЕ ОТ MAP</b> - список может содержать несколько свойств с одним и тем же ключом и значением.
 * При этом сохраняется порядок добавления таких свойств.<br/><br/>
 * Объекты данного класса являются не изменяемыми.
 */
public class Params {

    /**
     * Создает и возвращает объект используемый для создания экземпляра Params со всеми необходимыми данными.
     * @return объект для создания экземпляра Params.
     */
    public static Builder newBuilder() {
        return new Builder();
    }


    private final List<Param> params;

    private Params(List<Param> params) {
        if(params.stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("Params can't contains null");
        if(params.stream().anyMatch(param -> param.key() == null || param.key().isBlank()))
            throw new IllegalArgumentException("Params key can't be blank or null");

        this.params = new ArrayList<>(params);
    }

    /**
     * Возвращает неизменяемый список всех свойств в виде пар ключ-значение.
     * @return неизменяемый список всех свойств в виде пар ключ-значение.
     */
    public List<Param> getParams() {
        return Collections.unmodifiableList(params);
    }

    /**
     * Проверяет - содержит ли данный объект хотя бы одно свойство с указанным ключом.
     * @param key ключ искомого свойства.
     * @return true - если описанное выше условие выполняется, иначе - false.
     */
    public boolean hasParam(String key) {
        return params.stream().anyMatch(param -> param.key().equals(key));
    }

    /**
     * Проверяет - содержит ли данный объект хотя бы одно свойство для каждого из указанных ключей.
     * @param keys ключи искомых свойств.
     * @return true - если описанное выше условие выполняется, иначе - false.
     */
    public boolean hasAllParams(String... keys) {
        Set<String> keysSet = params.stream().map(Param::key).collect(Collectors.toSet());
        return Arrays.stream(keys).allMatch(keysSet::contains);
    }

    /**
     * Находит и возвращает значение первого свойства имеющего заданный ключ. Если нет свойства с таким
     * ключом - возвращает пустой Optional.
     * @param key ключ искомого свойства.
     * @return значение первого свойства имеющего заданный ключ или пустой Optional.
     */
    public Optional<Object> findFirstValueByKey(String key) {
        return findFirstByKey(key).map(Param::value);
    }

    /**
     * Находит значение первого свойства имеющего заданный ключ, а затем преобразует его к заданному типу
     * и возвращает. Если нет свойства с таким ключом - возвращает пустой Optional.
     * @param key ключ искомого свойства.
     * @param type тип к которому преобразуется возвращаемое значение искомого свойства.
     * @return значение первого свойства имеющего заданный ключ или пустой Optional.
     * @throws ClassCastException если тип значения свойства не удалось преобразовать к указанному типу.
     */
    public <T> Optional<T> findFirstValueByKeyAs(String key, Class<T> type) {
        return findFirstByKey(key).map(param -> param.valueAs(type));
    }

    /**
     * Находит и возвращает первое свойство имеющее заданный ключ. Если нет свойства с таким ключом -
     * возвращает пустой Optional.
     * @param key ключ искомого свойства.
     * @return первое свойство имеющее заданный ключ или пустой Optional.
     */
    public Optional<Param> findFirstByKey(String key) {
        return params.stream().
                filter(param -> param.key().equals(key)).
                findFirst();
    }

    /**
     * Находит и возвращает значения всех свойств имеющих заданный ключ. Если нет ни одного свойства с заданным
     * ключом - возвращает пустой список.
     * @param key ключ значений искомых свойств.
     * @return значения всех свойств имеющих заданный ключ.
     */
    public List<Object> findAllValuesByKey(String key) {
        return params.stream().
                filter(param -> param.key().equals(key)).
                map(Param::value).
                collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Находит и возвращает значения всех свойств имеющих заданный ключ, предварительно преобразуя каждое
     * значение к заданному типу. Если нет ни одного свойства с заданным ключом - возвращает пустой список.
     * @param key ключ значений искомых свойств.
     * @param type тип, к которому преобразуются возвращаемые значения искомых свойств.
     * @return значения всех свойств имеющих заданный ключ.
     * @throws ClassCastException если хотя бы одно из возвращаемых значений имеет тип отличный от указанного.
     */
    public <T> List<T> findAllValuesByKeyAs(String key, Class<T> type) {
        return params.stream().
                filter(param -> param.key().equals(key)).
                map(param -> param.valueAs(type)).
                collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Находит и возвращает все свойства, которые имеют заданный ключ. Если нет ни одного свойства с заданным
     * ключом - возвращает пустой список. Изменения вносимые в возвращаемый список не влияют на состояние
     * данного объекта.
     * @param key ключ искомых свойств.
     * @return все свойства, которые имеют заданный ключ.
     */
    public List<Param> findAllByKey(String key) {
        return params.stream().
                filter(param -> param.key().equals(key)).
                collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Проверяет - содержит ли данный объект свойство с указанным ключом и значением.
     * @param key искомый ключ.
     * @param value искомое значение.
     * @return true - если описанное выше условие выполняется, иначе - false.
     */
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


    /**
     * Реализация паттерна Builder для создания объектов {@link Params}. Один и тот же объект
     * может использоваться для создания разных и независимых(с точки зрения разделяемых данных) списков свойств.
     */
    public static class Builder {

        private final List<Param> params;

        private Builder() {
            this.params = new ArrayList<>();
        }

        /**
         * Заменяет все ранее заданные свойства указанным списком свойств.
         * @param params список свойств.
         * @return ссылку на этот же объект.
         */
        public Builder setParams(List<Param> params) {
            if(params != null) {
                this.params.clear();
                this.params.addAll(params);
            }
            return this;
        }

        /**
         * Добавляет указанное свойство.
         * @param param добавляемое свойство.
         * @return ссылку на этот же объект.
         */
        public Builder addParam(Param param) {
            params.add(param);
            return this;
        }

        /**
         * Добавляет указанное свойство заданное в виде пары ключ-значение.
         * @param key ключ добавляемого свойства.
         * @param value значение добавляемого свойства.
         * @return ссылку на этот же объект.
         */
        public Builder addParam(String key, Object value) {
            params.add(new Param(key, value));
            return this;
        }

        /**
         * Ищет первое свойство имеющее такой же ключ, как у задаваемого свойства. Если такое свойство
         * есть - заменяет его на указанное. Иначе - добавляет указанное свойство.
         * @param param задаваемое свойство.
         * @return ссылку на этот же объект.
         */
        public Builder replaceFirstParamOrAdd(Param param) {
            int index = IntStream.range(0, params.size()).
                    filter(i -> Objects.equals(params.get(i).key(), param.key())).
                    findFirst().
                    orElse(-1);
            if(index == -1) params.add(param);
            else params.set(index, param);
            return this;
        }

        /**
         * Ищет первое свойство имеющее указанный ключ. Если такое свойство есть - заменяет его на
         * указанное свойство в виде пары ключ-значение. Иначе - добавляет указанное свойство.
         * @param key ключ задаваемого свойства.
         * @param value значение задаваемого свойства.
         * @return ссылку на этот же объект.
         */
        public Builder replaceFirstParamOrAdd(String key, Object value) {
            return replaceFirstParamOrAdd(new Param(key, value));
        }

        /**
         * Создает и возвращает новый неизменяемый список свойств.
         * @return неизменяемый список свойств.
         */
        public Params build() {
            return new Params(params);
        }

    }

}
