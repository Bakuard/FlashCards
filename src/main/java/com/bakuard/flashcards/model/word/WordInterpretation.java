package com.bakuard.flashcards.model.word;

import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Интерпретация к слову.
 */
@Table("words_interpretations")
public class WordInterpretation {

    @Column("value")
    @NotBlank(message = "WordInterpretation.value.notBlank")
    private String value;
    @Transient
    private final List<OuterSource> outerSource;

    /**
     * Создает интерпретацию к слову.
     * @param value значение интерпретации к слову.
     */
    @PersistenceCreator
    public WordInterpretation(String value) {
        this.value = value;
        this.outerSource = new ArrayList<>();
    }

    /**
     * Выполняет глубокое копирование интерпретации к слову.
     * @param other копируемая интерпретация к слову.
     */
    public WordInterpretation(WordInterpretation other) {
        this.value = other.value;
        this.outerSource = new ArrayList<>(other.outerSource);
    }

    /**
     * Возвращает значение интерпретации к слову.
     * @return значение интерпретации к слову.
     */
    public String getValue() {
        return value;
    }

    /**
     * Возвращает данные обо всех внешних источниках, которые вернули данную интерпретацию слова.
     * @return данные обо всех внешних источниках, которые вернули данную интерпретацию слова.
     */
    public List<OuterSource> getOuterSource() {
        return outerSource;
    }

    /**
     * Возвращает дату последнего обновления интерпретации слова из внешнего сервиса с указанным именем. Если
     * среди внешних сервисов, из которых получена данная интерпретация, нет указанного сервиса - возвращает
     * пустой Optional.
     * @param outerSourceName имя внешнего сервиса используемого для перевода примера к слову.
     * @return дату последнего обновления перевода данного примера из внешнего сервиса с указанным именем
     */
    public Optional<LocalDate> getRecentUpdateDate(String outerSourceName) {
        return outerSource.stream().
                filter(outerSource -> outerSource.sourceName().equals(outerSourceName)).
                findFirst().
                map(OuterSource::recentUpdateDate);
    }

    /**
     * Проверяет - есть ли среди внешних сервисов, из которых получено данная интерпретация, внешний сервис
     * с указанным именем.
     * @param outerSourceName имя внешнего сервиса.
     * @return true - если среди сервисов, из которых получено данная интерпретация, есть сервис с указанным именем,
     *         иначе - false.
     */
    public boolean hasOuterSource(String outerSourceName) {
        return outerSource.stream().
                anyMatch(outerSource -> outerSource.sourceName().equals(outerSourceName));
    }

    /**
     * Выполняет слияние текущей интерпретации с переданной, а именно: <br/>
     * 1. если интерпретация other содержит данные о внешних сервисах, которых нет у данной интерпретации - 
     *    то эти данные будут добавлены к текущей интерпретации. <br/>
     * 2. если текущая интерпретация содержит данные о внешних сервисах, которых нет у интерпретации other - 
     *    то эти данные остаются без изменений. <br/>
     * 3. если данная интерпретация имеет такие же данные о внешних сервисах, что и интерпретация other - 
     *    то такие данные данной интерпретации будут заменены соответствующими данными из интерпретации other.
     * @param other интерпретация, данные которой сливаются с данными текущей интерпретации по описанному
     *              выше алгоритму.
     * @return true - если значение данной интерпретации и значение интерпретации other совпадают,
     *         иначе - false.
     */
    public boolean merge(WordInterpretation other) {
        boolean isMerged = value.equals(other.value);
        if(isMerged) {
            for(int i = 0; i < other.outerSource.size(); i++) {
                OuterSource otherInfo = other.outerSource.get(i);
                boolean isFind = false;
                int index = 0;
                for(int j = 0; j < outerSource.size() && !isFind; j++) {
                    isFind = outerSource.get(j).sourceName().equals(otherInfo.sourceName());
                    index = j;
                }
                if(isFind) outerSource.set(index, otherInfo);
                else outerSource.add(otherInfo);
            }
        }
        return isMerged;
    }

    /**
     * Добавляет к интерпретации слова данные о новом внешнем сервисе, который вернул её для относящегося к ней слова.
     * @param info данные о новом внешнем сервисе, который вернул интерпретацию для относящегося к ней слова.
     * @return ссылку на этот же объект.
     */
    public WordInterpretation addSourceInfo(OuterSource info) {
        outerSource.add(info);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordInterpretation that = (WordInterpretation) o;
        return Objects.equals(value, that.value) && Objects.equals(outerSource, that.outerSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, outerSource);
    }

    @Override
    public String toString() {
        return "WordInterpretation{" +
                "value='" + value + '\'' +
                ", sourceInfo=" + outerSource +
                '}';
    }

}
