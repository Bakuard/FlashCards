package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.model.word.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Отвечает за сохранение, извлечение и удаление слов из постоянного хранилища.
 * @see Word
 */
@Repository
public interface WordRepository extends PagingAndSortingRepository<Word, UUID>,
        ListCrudRepository<Word, UUID> {

    /**
     * Возвращает слово с идентификатором wordId из словаря пользователя с идентификатором userId.
     * Если нет пользователя с таким идентификатором или в словаре указанного пользователя нет слова с
     * таким идентификатором - возвращает пустой Optional.
     * @param userId идентификатор пользователя, к словарю которого относится слово
     * @param wordId идентификатор слова
     * @return слово или пустой Optional.
     */
    @Query("select * from words where user_id = :userId and word_id = :wordId;")
    public Optional<Word> findById(UUID userId, UUID wordId);

    /**
     * Возвращает выборку слов из словаря пользователя с идентификатором userId, редакционное расстояние между
     * значениями которых и значением value не превышает maxDistance. Все слова в выборке упорядочены в
     * порядке возрастания редакционного расстояния относительно заданного значения value, а затем в
     * лексикографическом порядке. Если нет ни одного слова удовлетворяющего описанному условию - возвращает
     * пустой список.
     * @param userId идентификатор пользователя, из слов которого делается выборка
     * @param value искомое значение слова
     * @param maxDistance максимальное значение редакционного расстояния
     * @param limit максимальное кол-во слов в возвращаемом списке
     * @param offset кол-во пропущенных слов от начала исходной выборки, которые не будут включены в список
     * @return список слов.
     */
    @Query("""
            select * from words
                where user_id = :userId and distance(:value, value, :maxDistance) != -1
                order by value limit :limit offset :offset;
            """)
    public List<Word> findByValue(UUID userId, String value, int maxDistance, long limit, long offset);

    /**
     * Возвращает выборку слов из словаря пользователя с идентификатором userId, где хотя бы один из переводов
     * каждого слова равен значению translate. Все возвращаемые слова отсортированы в лексикографическом порядке.
     * Если нет ни одного слова удовлетворяющего описанному условию - возвращает пустой список.
     * @param userId идентификатор пользователя, из слов которого делается выборка
     * @param translate один из возможных переводов искомых слов
     * @param limit максимальное кол-во слов в возвращаемом списке
     * @param offset кол-во пропущенных слов от начала исходной выборки, которые не будут включены в список
     * @return список слов.
     */
    @Query("""
            select * from words
                where words.word_id in (
                    select words.word_id from words
                        inner join words_translations
                            on  words.word_id = words_translations.word_id
                                and words.user_id = :userId
                                and words_translations.value = :translate
                )
                order by words.value
                limit :limit offset :offset;
            """)
    public List<Word> findByTranslate(UUID userId, String translate, long limit, long offset);

    /**
     * Удаляет слово с идентификатором wordId из словаря пользователя с идентификатором userId и
     * возвращает true. Если не существует пользователя с таким userId или в словаре данного пользователя нет
     * слова с идентификатором wordId - возвращает false не изменяя постоянное хранилище.
     * @param userId идентификатор пользователя, к словарю которого относится слово
     * @param wordId идентификатор слова
     * @return true - если указанный пользователь существует и существовало указанное слово в его словаре,
     *         иначе - false.
     */
    @Modifying
    @Query("delete from words where user_id = :userId and word_id = :wordId;")
    public boolean deleteById(UUID userId, UUID wordId);

    /**
     * Проверяет - существует ли пользователь с идентификатором userId и если да, то относится ли слово
     * с идентификатором wordId к его словарю.
     * @param userId идентификатор пользователя, к словарю которого относится слово
     * @param wordId идентификатор слова
     * @return true - если описанное выше условие выполняется, иначе - false
     */
    @Query("select exists(select 1 from words where user_id = :userId and word_id = :wordId);")
    public boolean existsById(UUID userId, UUID wordId);

    /**
     * Возвращает кол-во всех слов в словаре пользователя userId.
     * @param userId идентификатор пользователя.
     * @return кол-во всех слов в словаре пользователя userId.
     */
    @Query("select count(*) from words where user_id = :userId")
    public long count(UUID userId);

    /**
     * Возвращает кол-во всех слов из словаря пользователя userId доступных для повторения с английского
     * на родной язык в указанную дату.
     * @param userId идентификатор пользователя, из слов которого делается выборка
     * @param date дата для которой подбираются доступные для повторения слова
     * @return кол-во слов доступных для повторения
     */
    @Query("""
            select count(*) from words
             where user_id = :userId and (last_date_of_repeat_from_english + repeat_interval_from_english) <= :date;
            """)
    public long countForRepeatFromEnglish(UUID userId, LocalDate date);

    /**
     * Возвращает кол-во всех слов из словаря пользователя userId доступных для повторения с родного
     * на английский язык в указанную дату.
     * @param userId идентификатор пользователя, из слов которого делается выборка
     * @param date дата для которой подбираются доступные для повторения слова
     * @return кол-во слов доступных для повторения
     */
    @Query("""
            select count(*) from words
             where user_id = :userId and (last_date_of_repeat_from_native + repeat_interval_from_native) <= :date;
            """)
    public long countForRepeatFromNative(UUID userId, LocalDate date);

    /**
     * Возвращает кол-во всех слов из словаря пользователя с идентификатором userId, редакционное расстояние
     * между значениями которых и значением value не превышает maxDistance.
     * @param userId идентификатор пользователя, из слов которого делается выборка
     * @param value искомое значение слова
     * @param maxDistance максимальное значение редакционного расстояния
     * @return кол-во слов, редакционное расстояние между значениями которых и значением value не превышает
     *         maxDistance.
     */
    @Query("""
            select count(*) from (
               select value
                   from words
                   where user_id = :userId and distance(:value, value, :maxDistance) != -1
            )
            """)
    public long countForValue(UUID userId, String value, int maxDistance);

    /**
     * Возвращает кол-во всех слов из словаря пользователя с идентификатором userId, где хотя бы один из
     * переводов каждого слова равен значению translate.
     * @param userId идентификатор пользователя, из слов которого делается выборка
     * @param translate один из возможных переводов искомых слов
     * @return кол-во слов, у которых хотя бы один из переводов равен translate
     */
    @Query("""
            select count(*)
                from words
                inner join words_translations
                    on words_translations.word_id = words.word_id
                       and words_translations.value = :translate
                       and words.user_id = :userId;
            """)
    public long countForTranslate(UUID userId, String translate);

    /**
     * Возвращает порядковый номер первого из слов начинающегося на указанную букву. Нумерация слов начинается
     * с 0. Слова сортируются в лексикографическом порядке.
     * @param userId идентификатор пользователя, из слов которого делается выборка
     * @param firstCharacter первый символ искомого слова
     * @return порядковый номер первого из слов начинающегося на указанную букву
     */
    @Query("""
            select COALESCE(min(row_number) - 1, -1) as result
                from (
                    select words.value as word_value, row_number() over (order by value) as row_number
                        from words
                        where words.user_id = :userId
                ) as words_with_row_number
                where lower(left(word_value, 1)) = lower(:firstCharacter);
            """)
    public long getWordIndexByFirstCharacter(UUID userId, String firstCharacter);

    /**
     * Возвращает часть всех слов из словаря пользователя с идентификатором userId. Если в словаре пользователя
     * нет ни одного слова - возвращает пустой список.
     * @param userId идентификатор пользователя, из слов которого делается выборка
     * @param pageable параметры пагинации и сортировки слов
     * @return страницу слов.
     */
    public Page<Word> findByUserId(UUID userId, Pageable pageable);

    /**
     * Возвращает слова доступные для повторения в указанную дату с английского языка на родной язык пользователя.
     * Все слова берутся из словаря пользователя с идентификатором userId. Порядок сортировки слов -
     * лексикографический. Если нет слов доступных для повторения в текущую дату - возвращает пустой список.
     * @param userId идентификатор пользователя, из слов которого делается выборка
     * @param date дата для которой подбираются доступные для повторения слова
     * @param limit максимальное кол-во слов в возвращаемом списке
     * @param offset кол-во пропущенных слов от начала исходной выборки, которые не будут включены в список
     * @return список слов
     */
    @Query("""
            select * from words
             where user_id = :userId and (last_date_of_repeat_from_english + repeat_interval_from_english) <= :date
             order by value limit :limit offset :offset;
            """)
    public List<Word> findAllForRepeatFromEnglish(UUID userId, LocalDate date, long limit, long offset);

    /**
     * Возвращает слова доступные для повторения в текущую дату с родного языка пользователя на английский язык.
     * Все слова берутся из словаря пользователя с идентификатором userId. Порядок сортировки выборки слов -
     * лексикографический. Если нет слов доступных для повторения в текущую дату - возвращает пустой список.
     * @param userId идентификатор пользователя, из слов которого делается выборка
     * @param date дата для которой подбираются доступные для повторения слова
     * @param limit максимальное кол-во слов в возвращаемом списке
     * @param offset кол-во пропущенных слов от начала исходной выборки, которые не будут включены в список
     * @return список слов
     */
    @Query("""
            select * from words
             where user_id = :userId and (last_date_of_repeat_from_native + repeat_interval_from_native) <= :date
             order by value limit :limit offset :offset;
            """)
    public List<Word> findAllForRepeatFromNative(UUID userId, LocalDate date, long limit, long offset);

}
