package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.model.word.supplementation.SupplementedWord;
import com.bakuard.flashcards.validation.exception.NotUniqueEntityException;
import com.bakuard.flashcards.validation.exception.UnknownEntityException;

import java.util.Optional;
import java.util.UUID;

/**
 * Отвечает за буферизацию данных слова (транскрипции, толкования, переводы и переводы примеров)
 * полученных из внешних сервисов.
 */
public interface WordOuterSourceBuffer {

    /**
     * Сохраняет в буфер данные одного конкретного слова (транскрипции, толкования, переводы и
     * переводы примеров) полученные из одного определенного внешнего сервиса.
     * @param word сохраняемые данные некоторого слова.
     * @throws NullPointerException если word равен null
     * @throws NotUniqueEntityException если {@link SupplementedWord#isNew()} возвращает false и
     *                                  в буфере уже есть данные этого слова ({@link SupplementedWord#getValue()})
     *                                  из указанного внешнего сервиса ({@link SupplementedWord#getOuterSourceName()})
     * @throws UnknownEntityException если {@link SupplementedWord#isNew()} возвращает true и
     *                                в буфере нет данных этого слова ({@link SupplementedWord#getValue()})
     *                                из указанного внешнего сервиса ({@link SupplementedWord#getOuterSourceName()})
     */
    public void save(SupplementedWord word);

    /**
     * Возвращает из буфера данные одного конкретного слова, а именно: <br/>
     * 1. транскрипции, толкования или переводы слова - по значению слова и наименованию внешнего сервиса,
     *    из которого они були получены. Если таких данных нет - метод вернет пустой Optional.<br/>
     * 2. переводы примеров к слову, которые связанны с указанным словом из словаря указанного пользователя,
     *    и при этом были получены из внешнего сервиса с указанным именем. Если таких данных нет, метод
     *    {@link SupplementedWord#getExamples()} будет возвращать пустой список.<br/><br/>
     * Метод не гарантирует возвращение всех перечисленных данных (транскрипции, толкования, переводы слова или
     * переводы примеров к слову). Что именно из этого он вернет - зависит от внешнего сервиса. <br/>
     * @param outerSourceName наименование внешнего сервиса
     * @param wordValue значение слова
     * @param examplesOwnerId идентификатор пользователя, к которому относятся загружаемые примеры к слову
     * @return данные некоторого слова (транскрипции, толкования, переводы и переводы примеров).
     * @throws NullPointerException если хотя бы один из аргументов равен null.
     */
    public Optional<SupplementedWord> findByWordValueAndOuterSource(String outerSourceName,
                                                                    String wordValue,
                                                                    UUID examplesOwnerId);

    /**
     * Удаляет из буфера все переводы к примерам, которые больше не используются ни для одного слова.
     * Если таких нет - ничего не делает.
     * @return кол-во удаленных примеров
     */
    public int deleteUnusedExamples();

}
