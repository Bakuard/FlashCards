package com.bakuard.flashcards.model.word;

import com.bakuard.flashcards.validation.NotBlankOrNull;
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
 * Пример к слову.
 */
@Table("words_examples")
public class WordExample {

    @Column("origin")
    @NotBlank(message = "WordExample.origin.notBlank")
    private String origin;
    @Column("translate")
    @NotBlankOrNull(message = "WordExample.translate.notBlankOrNull")
    private String translate;
    @Column("note")
    @NotBlankOrNull(message = "WordExample.note.notBlankOrNull")
    private String note;
    @Transient
    private final List<ExampleOuterSource> outerSource;

    /**
     * Создает пример к слову.
     * @param origin значение примера к слову на английском языке.
     * @param translate перевод примера на родной язык пользователя.
     * @param note примечание к примеру добавляемое пользователем.
     */
    @PersistenceCreator
    public WordExample(String origin, String translate, String note) {
        this.origin = origin;
        this.translate = translate;
        this.note = note;
        this.outerSource = new ArrayList<>();
    }

    /**
     * ВЫполняет глубокое копирование переданного примера к слову.
     * @param other копируемый пример к слову.
     */
    public WordExample(WordExample other) {
        this.origin = other.origin;
        this.translate = other.translate;
        this.note = other.note;
        this.outerSource = new ArrayList<>(other.outerSource);
    }

    /**
     * Возвращает значение примера к слову на английском языке.
     * @return значение примера к слову на английском языке.
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Возвращает перевод к примеру слова.
     * @return перевод к примеру слова.
     */
    public String getTranslate() {
        return translate;
    }

    /**
     * Возвращает примечание к примеру заданное пользователем.
     * @return примечание к примеру.
     */
    public String getNote() {
        return note;
    }

    /**
     * Возвращает данные обо всех внешних источниках, которые использователись для перевода данного примера.
     * @return данные обо всех внешних источников, которые использователись для перевода данного примера.
     */
    public List<ExampleOuterSource> getOuterSource() {
        return outerSource;
    }

    /**
     * Вовзращает дату последнего обновления перевода данного примера из внешнего сервиса с указанным именем. Если
     * среди переводов данного примера нет перевода полученного из указанного сервиса - возвращает пустой Optional.
     * @param outerSourceName имя внешнего сервиса используемого для перевода примера к слову.
     * @return дату последнего обновления перевода данного примера из внешнего сервиса с указанным именем
     */
    public Optional<LocalDate> getRecentUpdateDate(String outerSourceName) {
        return outerSource.stream().
                filter(outerSource -> outerSource.sourceName().equals(outerSourceName)).
                findFirst().
                map(ExampleOuterSource::recentUpdateDate);
    }

    public WordExample setTranslate(String translate) {
        this.translate = translate;
        return this;
    }

    /**
     * Выполняет слияние данного примера c переданным примером, а именно: <br/>
     * 1. если {@link #getOrigin()} данного примера и примера other возвращает разные значения, то метод
     *    ничего не делает. Иначе выполняются нижеописанные шаги. <br/>
     * 2. если данный пример слова и пример other имеют переводы полученные из одних и тех же
     *    внешних сервисов, то такие переводы данного примера будут заменены соответствующими переводами
     *    примера other. <br/>
     * 3. если пример other иммеет переводы полученные из таких внешних сервисов, переводы из которых отсутствуют
     *    в данном примере, то эти переводы будут добавленны в данный пример.<br/>
     * 4. если данный пример имеет переводы полученные из таких внешних источников, переводы из которых
     *    отсутствуют в примере other, то эти переводы остаются без изменений.<br/>
     * 5. если данный пример не имеет перевода, то для него будет присвоено значение перевода примера other.
     * @param other пример, данные которого сливаются с данными текущего примера по описаннмоу выше алгоритму.
     * @return true - если значение данного примера на английском и аналогичное значение примера other совпадают,
     *         иначе - false.
     */
    public boolean mergeIfHasSameValue(WordExample other) {
        boolean isMerged = origin.equals(other.origin);
        if(isMerged) {
            for(int i = 0; i < other.outerSource.size(); i++) {
                ExampleOuterSource otherInfo = other.outerSource.get(i);
                boolean isFind = false;
                int index = 0;
                for(int j = 0; j < outerSource.size() && !isFind; j++) {
                    isFind = outerSource.get(j).sourceName().equals(otherInfo.sourceName());
                    index = j;
                }
                if(isFind) outerSource.set(index, otherInfo);
                else outerSource.add(otherInfo);
            }
            if(translate == null && !outerSource.isEmpty()) {
                translate = outerSource.get(0).translate();
            }
        }
        return isMerged;
    }

    /**
     * Добавляет к данному примеру перевод из нового внешнего сервиса и данные об этом сервисе.
     * @param info перевод из нового внешнего сервиса и данные об этом сервисе.
     * @return ссылку на этот же объект.
     */
    public WordExample addSourceInfo(ExampleOuterSource info) {
        outerSource.add(info);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordExample example = (WordExample) o;
        return Objects.equals(origin, example.origin) &&
                Objects.equals(translate, example.translate) &&
                Objects.equals(note, example.note) &&
                Objects.equals(outerSource, example.outerSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, translate, note, outerSource);
    }

    @Override
    public String toString() {
        return "WordExample{" +
                "origin='" + origin + '\'' +
                ", translate='" + translate + '\'' +
                ", note='" + note + '\'' +
                ", sourceInfo=" + outerSource +
                '}';
    }

}
