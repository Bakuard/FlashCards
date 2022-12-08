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
 * Интерпритация к слову.
 */
@Table("words_interpretations")
public class WordInterpretation {

    @Column("value")
    @NotBlank(message = "WordInterpretation.value.notBlank")
    private String value;
    @Transient
    private final List<OuterSource> outerSource;

    /**
     * Создает интерпритацию к слову.
     * @param value значение интерпритации к слову.
     */
    @PersistenceCreator
    public WordInterpretation(String value) {
        this.value = value;
        this.outerSource = new ArrayList<>();
    }

    /**
     * Выполняет глубокое копирование интерпритации к слову.
     * @param other копируемая интерпритация к слову.
     */
    public WordInterpretation(WordInterpretation other) {
        this.value = other.value;
        this.outerSource = new ArrayList<>(other.outerSource);
    }

    /**
     * Возвращает значение интерприатции к слову.
     * @return значение интерприатции к слову.
     */
    public String getValue() {
        return value;
    }

    /**
     * Возвращает данные обо всех внешних источниках, которые вернули данную интерпритацию слова.
     * @return данные обо всех внешних источниках, которые вернули данную интерпритацию слова.
     */
    public List<OuterSource> getOuterSource() {
        return outerSource;
    }

    /**
     * Возвращает дату последнего обновления интерпритации слова из внешнего сервиса с указанным именем. Если
     * среди внешних сервисов, из которых полученна данная интерпритация, нет указанного сервиса - возвращает
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
     * Проверяет - есть ли среди внешних сервисов, из которых полученно данная интерпритация, внешний сервис
     * с указаным именем.
     * @param outerSourceName имя внешнего сервиса.
     * @return true - если среди сервисов, из которых полученно данная интерпритация, есть сервис с указанным именем,
     *         иначе - false.
     */
    public boolean hasOuterSource(String outerSourceName) {
        return outerSource.stream().
                anyMatch(outerSource -> outerSource.sourceName().equals(outerSourceName));
    }

    /**
     * Выполняет слияние текущей интерпритации с переданной, а именно: <br/>
     * 1. если интерпритация other содержит данные о внешних сервисах (из которых она была получена),
     *    которых нет у данной интерпритации - то эти данные будут добавлены к текущей интерпритации. <br/>
     * 2. если текущая интерпритация содержит данные о внешних сервисах (из которых она была получена),
     *    которых нет у интерпритации other - то эти данные остаются без изменений. <br/>
     * 3. если данная интерпритация имеет такие же данные о внешних сервисах (из которых она была получена),
     *    что и интерпритация other - то такие данные данной интерпритации будут заменены соответствующими
     *    данными из интерпритации other.
     * @param other интерпритация, данные которой сливаются с данными текущей интерпритации по описаннмоу
     *              выше алгоритму.
     * @return true - если значение данной интерпритации и значение интерпритации other совпадают,
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
     * Добавляет к интерпритации слова данные о новом внешнем сервисе, который вернул её для относящегося к ней слова.
     * @param info данные о новом внешнем сервисе, который вернул интерпритацию для относящегося к ней слова.
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
