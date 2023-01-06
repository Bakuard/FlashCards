package com.bakuard.flashcards.model.auth.credential;

import com.bakuard.flashcards.model.auth.resource.Param;
import com.bakuard.flashcards.model.auth.resource.Params;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Представление пользователя с точки зрения процесса авторизации, т.е. содержит данные некоторого пользователя,
 * который делает запрос требующий авторизации. <br/><br/>
 * Каждый пользователь, с точки зрения процесса авторизации, может обладать следующими данными:<br/>
 * 1. id - уникальный идентификатор. Обязательное поле. <br/>
 * 2. detail - представление пользователя с точки зрения бизнес логики. Должно быть представлено в виде одного
 *             объекта. Опциональное поле. <br/>
 * 3. params - любые дополнительные данные, которых нет в представлении пользователя с точки зрения
 *             бизнес процессов, или их проще получить напрямую из контекста авторизуемого запроса.
 *             Опциональное поле.<br/>
 */
public class Principal {

    /**
     * Создает и возвращает пользователя с указанным идентификатором.
     * @param id идентификатор пользователя.
     * @return новый объект пользователя.
     * @throws NullPointerException если id равен null.
     */
    public static Principal of(UUID id) {
        return newBuilder().setId(id).build();
    }

    /**
     * Создает и возвращает объект используемый для создания экземпляра Principal со всеми необходимыми данными.
     * @return объект для создания экземпляра Principal.
     */
    public static Builder newBuilder() {
        return new Builder();
    }


    private final UUID id;
    private final Params params;

    private Principal(UUID id, Params params) {
        this.id = Objects.requireNonNull(id, "Principal id can't be null.");
        this.params = params;
    }

    /**
     * Возвращает уникальный идентификатор пользователя.
     * @return уникальный идентификатор пользователя.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Проверяет - было ли указанно представление пользователя с точки зрения бизнес логики.
     * @return true - если описанное выше условие выполняется, иначе - false.
     */
    public boolean hasDetail() {
        return params.hasParam("detail");
    }

    /**
     * Проверяет - имеет ли представление пользователя с точки зрения бизнес процессов указанный тип данных.
     * @param type предполагаемый тип данных для представления пользователя с точки зрения бизнес логики.
     * @return true - если описанное выше условие выполняется, иначе - false.
     */
    public boolean detailTypeIs(Class<?> type) {
        return params.findFirstByKey("detail").
                map(param -> param.isValueType(type)).
                orElse(false);
    }

    /**
     * Возвращает представление пользователя с точки зрения бизнес процессов предварительно преобразуя его
     * в указанный тип данных. Если указанные данные не были заданы - возвращает пустой Optional.
     * @param type предполагаемый тип данных для представления пользователя с точки зрения бизнес логики.
     * @return представление пользователя с точки зрения бизнес процессов.
     * @throws ClassCastException если не удалось преобразовать возвращаемый результат к указанному типу.
     */
    public <T> Optional<T> getDetailAs(Class<T> type) {
        return params.findFirstValueByKeyAs("detail", type);
    }

    /**
     * Возвращает дополнительные данные о пользователе в виде упорядоченного набора
     * пар ключ-значение (см. {@link Params}).
     * @return дополнительные данные о пользователе в виде упорядоченного набора пар ключ-значение.
     */
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


    /**
     * Реализация паттерна Builder для создания объектов {@link Principal}. Один и тот же объект
     * может использоваться для создания разных и независимых(с точки зрения разделяемых данных) пользователей.
     */
    public static class Builder {

        private UUID id;
        private final Params.Builder params;

        private Builder() {
            this.id = UUID.randomUUID();
            this.params = Params.newBuilder();
        }

        /**
         * Устанавливает идентификатор пользователя.
         * @see Principal
         * @param id идентификатор пользователя.
         * @return ссылку на этот же объект.
         */
        public Builder setId(UUID id) {
            this.id = id == null ? UUID.randomUUID() : id;
            return this;
        }

        /**
         * Устанавливает данные о пользователе с точки зрения бизнес логики.
         * @see Principal
         * @param detail данные о пользователе с точки зрения бизнес логики.
         * @return ссылку на этот же объект.
         */
        public Builder setDetail(Object detail) {
            params.addParam("detail", detail);
            return this;
        }

        /**
         * Добавляет произвольное свойство к создаваемому пользователю.
         * @see Param
         * @see Principal
         * @param param произвольное свойство к создаваемому пользователю.
         * @return ссылку на этот же объект.
         */
        public Builder addParam(Param param) {
            params.addParam(param);
            return this;
        }

        /**
         * Добавляет произвольное свойство к создаваемому пользователю.
         * @see Param
         * @see Principal
         * @param key ключ добавляемого свойства.
         * @param value значение добавляемого свойства.
         * @return ссылку на этот же объект.
         */
        public Builder addParam(String key, Object value) {
            params.addParam(key, value);
            return this;
        }

        /**
         * Создает и возвращает новый объект {@link Principal}.
         * @return новый объект {@link Principal}.
         */
        public Principal build() {
            return new Principal(id, params.build());
        }

    }

}
