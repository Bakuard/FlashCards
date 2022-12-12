package com.bakuard.flashcards.model.auth.resource;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Метаданные для некоторого типа ресурсов или какого-то конкретного ресурса, к которому требуется
 * регулировать доступ (для краткости, далее будет использоваться термин ресурс). <br/><br/>
 * Каждый ресурс описывается в виде списка пар ключ-значение называемых свойствами ({@link Param}).
 * Ресурс допускает добавлять любые свойства, а также имеет несколько опциональных свойств с
 * предопределенными ключами: action, payload и type. <br/>
 * 1. <i>action</i> - все свойства с этим ключом описывают набор обобщенных действий, который возможно выполнить
 * над ограничиваемом в доступе ресурсом. Все значения таких свойств имеют тип {@link Action}.<br/>
 * 2. <i>payload</i> - непосредственно объект, к которому регулируется доступ или любые другие данные. <br/>
 * 3. <i>type</i> - группа ресурсов объединенных вместе по некоторым признакам и рассматриваемых как единое целое.
 * Может иметь любое значение. Не требуется, чтобы это было имя класса или интерфейса.<br/><br/>
 * Объекты данного класса не изменяемы.
 */
public class Resource {

    /**
     * Создает и возвращает новый ресурс имеющий заданный тип.
     * @param type тип ресурса.
     * @return новый объект ресурса.
     */
    public static Resource of(String type) {
        return newBuilder().setType(type).build();
    }

    /**
     * Создает и возвращает новый ресурс имеющий заданный тип и полезную нагрузку. Любой из параметров
     * может принимать значение null.
     * @param type тип ресурса.
     * @param payload любой объект.
     * @return новый объект ресурса.
     */
    public static Resource of(String type, Object payload) {
        return Resource.newBuilder().
                setType(type).
                setPayload(payload).
                build();
    }

    /**
     * Создает и возвращает объект используемый для создания экземпляра Resource со всеми необходимыми данными.
     * @return объект для создания экземпляра Resource.
     */
    public static Builder newBuilder() {
        return new Builder();
    }


    private final Params params;

    private Resource(Params params) {
        this.params = params;
    }

    /**
     * Возвращает тип данного ресурса. Если тип не был указан - возвращает пустой Optional.
     * @return тип данного ресурса.
     */
    public Optional<String> getType() {
        return params.findFirstValueByKeyAs("type", String.class);
    }

    /**
     * Проверяет - равняется ли тип ресурса указанному. Если тип ресурса не был задан - возвращает false.
     * @param type предполагаемый тип ресурса.
     * @return true - если тип был задан и равняется указанному, иначе - false.
     */
    public boolean typeIs(String type) {
        return params.findFirstValueByKeyAs("type", String.class).
                map(t -> t.equals(type)).
                orElse(false);
    }

    /**
     * Проверяет - был ли задан тип для данного ресурса.
     * @return true - если тип был задан, иначе - false.
     */
    public boolean hasType() {
        return params.hasParam("type");
    }

    /**
     * Проверяет - была ли добавлена полезная нагрузка для данного ресурса.
     * @return true - если описанное выше условие выполняется, иначе - false.
     */
    public boolean containsPayload() {
        return params.hasParam("payload");
    }

    /**
     * Проверяет - имеет ли полезная нагрузка заданный тип или супер-тип. Если полезная нагрузка не добавлялась
     * к этому ресурсу - возвращает false.
     * @param type предполагаемый тип полезной нагрузки.
     * @return true - если полезная нагрузка задана и имеет указанный тип, иначе - false.
     */
    public boolean payloadTypeIs(Class<?> type) {
        return params.findFirstByKey("payload").
                map(param -> param.isValueType(type)).
                orElse(false);
    }

    /**
     * Проверяет - равняется ли полезная нагрузка указанному объекту. Если полезная нагрузка не задана -
     * возвращает false.
     * @param obj предполагаемая полезная нагрузка.
     * @return true - если полезная нагрузка задана и равняется указанному объекту, иначе - false.
     */
    public boolean payloadIsEqualTo(Object obj) {
        return params.findFirstByKey("payload").
                map(param -> Objects.equals(param.value(), obj)).
                orElse(false);
    }

    /**
     * Возвращает полезную нагрузку предварительно преобразованную к заданному типу. Если полезная нагрузка не была
     * задана - возвращает пустой Optional.
     * @param type тип к которому преобразуется полезная нагрузка.
     * @return полезная нагрузка, преобразованная к указанному типу.
     * @throws ClassCastException если не удалось преобразовать полезную нагрузку к указанному типу.
     */
    public <T> Optional<T> getPayloadAs(Class<T> type) {
        return params.findFirstValueByKeyAs("payload", type);
    }

    /**
     * Возвращает список всех действий, которые можно выполнить над ресурсом.
     * @return список всех действий, которые можно выполнить над ресурсом.
     */
    public List<Action> getActions() {
        return params.findAllValuesByKeyAs("action", Action.class);
    }

    /**
     * Проверяет - можно и выполнить над данным ресурсом указанное действие.
     * @param action действие над ресурсом.
     * @return true - если описанное выше условие выполняется, иначе - false.
     */
    public boolean hasAction(Action action) {
        return params.containsValueByKey("action", action);
    }

    /**
     * Проверяет - можно и выполнить над данным ресурсом указанное действие.
     * @param action имя действия, которое можно выполнить над ресурсом.
     * @return true - если описанное выше условие выполняется, иначе - false.
     */
    public boolean hasAction(String action) {
        return hasAction(new Action(action));
    }

    /**
     * Возвращает список всех свойств описывающих ресурс.
     * @return список всех свойств описывающих ресурс.
     */
    public Params getParams() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return params.equals(resource.params);
    }

    @Override
    public int hashCode() {
        return params.hashCode();
    }

    @Override
    public String toString() {
        return "Resource{" + params + '}';
    }


    /**
     * Реализация паттерна Builder для создания объектов {@link Resource}. Один и тот же объект
     * может использоваться для создания разных и независимых(с точки зрения разделяемых данных) ресурсов.
     */
    public static class Builder {

        private final Params.Builder params;

        private Builder() {
            this.params = Params.newBuilder();
        }

        /**
         * Устанавливает тип ресурса.
         * @see Resource
         * @param type тип ресурса.
         * @return ссылка на этот же объект.
         */
        public Builder setType(String type) {
            params.replaceFirstParamOrAdd("type", type);
            return this;
        }

        /**
         * Устанавливает полезную нагрузку.
         * @param payload полезная нагрузка.
         * @return ссылку на этот же объект.
         * @see Resource
         */
        public Builder setPayload(Object payload) {
            params.replaceFirstParamOrAdd("payload", payload);
            return this;
        }

        /**
         * Добавляет описание некоторого действия над ресурсом.
         * @see Action
         * @see Resource
         * @param action действие над ресурсом.
         * @return ссылку на этот же объект.
         */
        public Builder addAction(Action action) {
            params.addParam("action", action);
            return this;
        }

        /**
         * Добавляет описание некоторого действия над ресурсом.
         * @see Action
         * @see Resource
         * @param action наименование действия над ресурсом.
         * @return ссылку на этот же объект.
         */
        public Builder addAction(String action) {
            params.addParam("action", new Action(action));
            return this;
        }

        /**
         * Добавляет произвольное свойство к создаваемому ресурсу.
         * @see Param
         * @see Resource
         * @param param произвольное свойство к создаваемому ресурсу.
         * @return ссылку на этот же объект.
         */
        public Builder addParam(Param param) {
            params.addParam(param);
            return this;
        }

        /**
         * Добавляет произвольное свойство к создаваемому ресурсу.
         * @see Param
         * @see Resource
         * @param key ключ добавляемого свойства.
         * @param value значение добавляемого свойства.
         * @return ссылку на этот же объект.
         */
        public Builder addParam(String key, Object value) {
            params.addParam(key, value);
            return this;
        }

        /**
         * Создает и возвращает новый ресурс.
         * @see Resource
         * @return новый ресурс.
         */
        public Resource build() {
            return new Resource(params.build());
        }

    }

}
