package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.model.expression.Expression;
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
 * Отвечает за сохранение, извлечение и удаление устойчивых выражений из постоянного хранилища.
 * @see Expression
 */
@Repository
public interface ExpressionRepository extends PagingAndSortingRepository<Expression, UUID>,
        ListCrudRepository<Expression, UUID> {

    /**
     * Возвращает устойчивое выражение с идентификатором expressionId из словаря пользователя с идентификатором
     * userId. Если нет пользователя с таким идентификатором или в словаре указанного пользователя нет устойчивого
     * выражения с таким идентификатором - возвращает пустой Optional.
     * @param userId идентификатор пользователя, к словарю которого относится слово
     * @param expressionId идентификатор устойчивого выражения
     * @return слово или пустой Optional.
     */
    @Query("select * from expressions where user_id = :userId and expression_id = :expressionId;")
    public Optional<Expression> findById(UUID userId, UUID expressionId);

    /**
     * Возвращает выборку устойчивых выражений из словаря пользователя с идентификатором userId, редакционное
     * расстояние между значениями которых и значением value не превышает maxDistance. Все устойчивые выражения
     * в выборке упорядочены в порядке возрастания редакционного расстояния относительно заданного значения value,
     * а затем в лексикографическом порядке. Если нет ни одного устойчивого выражения удовлетворяющего описанному
     * условию - возвращает пустой список.
     * @param userId идентификатор пользователя, из устойчивых выражений которого делается выборка
     * @param value искомое значение устойчивого выражения
     * @param maxDistance максимальное значение редакционного расстояния
     * @param limit максимальное кол-во устойчивых выражений в возвращаемом списке
     * @param offset кол-во пропущенных устойчивых выражений от начала исходной выборки, которые не будут
     *               включены в список
     * @return список устойчивых выражений.
     */
    @Query("""
            select * from expressions
                where user_id = :userId and distance(:value, value, :maxDistance) != -1
                order by value limit :limit offset :offset;
            """)
    public List<Expression> findByValue(UUID userId, String value, int maxDistance, long limit, long offset);

    /**
     * Возвращает выборку устойчивых выражений из словаря пользователя с идентификатором userId, где хотя бы
     * один из переводов каждого выражения равен значению translate. Все возвращаемые устойчивые выражения
     * отсортированы в лексикографическом порядке. Если нет ни одного устойчивого выражения удовлетворяющего
     * описанному условию - возвращает пустой список.
     * @param userId идентификатор пользователя, из устойчивых выражений которого делается выборка
     * @param translate один из возможных переводов искомых устойчивых выражений
     * @param limit максимальное кол-во устойчивых выражений в возвращаемом списке
     * @param offset кол-во пропущенных устойчивых выражений от начала исходной выборки, которые не будут
     *               включены в список
     * @return список устойчивых выражений.
     */
    @Query("""    
            select * from expressions
                where expressions.expression_id in (
                    select expressions.expression_id from expressions
                        inner join expressions_translations
                            on  expressions.expression_id = expressions_translations.expression_id
                                and expressions.user_id = :userId
                                and expressions_translations.value = :translate
                )
                order by expressions.value
                limit :limit offset :offset;
            """)
    public List<Expression> findByTranslate(UUID userId, String translate, long limit, long offset);

    /**
     * Удаляет устойчивое выражение с идентификатором expressionId из словаря пользователя с идентификатором
     * userId и возвращает true. Если не существует пользователя с таким userId или в словаре данного
     * пользователя нет выражения с идентификатором expressionId - возвращает false не изменяя постоянное
     * хранилище.
     * @param userId идентификатор пользователя, к словарю которого относится устойчивое выражение
     * @param expressionId идентификатор устойчивого выражения
     * @return true - если указанный пользователь существует и существовало указанное выражение в его словаре,
     *         иначе - false.
     */
    @Modifying
    @Query("delete from expressions where user_id = :userId and expression_id = :expressionId;")
    public boolean deleteById(UUID userId, UUID expressionId);

    /**
     * Проверяет - существует ли пользователь с идентификатором userId и если да, то относится ли устойчивое
     * выражение с идентификатором expressionId к его словарю.
     * @param userId идентификатор пользователя, к словарю которого относится устойчивое выражение
     * @param expressionId идентификатор устойчивого выражения
     * @return true - если описанное выше условие выполняется, иначе - false
     */
    @Query("select exists(select 1 from expressions where user_id = :userId and expression_id = :expressionId);")
    public boolean existsById(UUID userId, UUID expressionId);

    /**
     * Возвращает кол-во всех устойчивых выражений в словаре пользователя userId.
     * @param userId идентификатор пользователя.
     * @return кол-во всех устойчивых выражений в словаре пользователя userId.
     */
    @Query("select count(*) from expressions where user_id = :userId")
    public long count(UUID userId);

    /**
     * Возвращает кол-во всех устойчивых выражений из словаря пользователя userId доступных для повторения с
     * английского на родной язык в указанную дату.
     * @param userId идентификатор пользователя, из устойчивых выражений которого делается выборка
     * @param date дата для которой подбираются доступные для повторения устойчивые выражения
     * @return кол-во устойчивых выражений доступных для повторения
     */
    @Query("""
            select count(*) from expressions
             where user_id = :userId and (last_date_of_repeat_from_english + repeat_interval_from_english) <= :date;
            """)
    public long countForRepeatFromEnglish(UUID userId, LocalDate date);

    /**
     * Возвращает кол-во всех устойчивых выражений из словаря пользователя userId доступных для повторения с
     * родного на английский язык в указанную дату.
     * @param userId идентификатор пользователя, из устойчивых выражений которого делается выборка
     * @param date дата для которой подбираются доступные для повторения устойчивые выражения
     * @return кол-во устойчивых выражений доступных для повторения
     */
    @Query("""
            select count(*) from expressions
             where user_id = :userId and (last_date_of_repeat_from_native + repeat_interval_from_native) <= :date;
            """)
    public long countForRepeatFromNative(UUID userId, LocalDate date);

    /**
     * Возвращает кол-во всех устойчивых выражений из словаря пользователя с идентификатором userId, редакционное
     * расстояние между значениями которых и значением value не превышает maxDistance.
     * @param userId идентификатор пользователя, из устойчивых выражений которого делается выборка
     * @param value искомое значение устойчивого выражения
     * @param maxDistance максимальное значение редакционного расстояния
     * @return кол-во устойчивых выражений, редакционное расстояние между значениями которых и значением value
     *         не превышает maxDistance.
     */
    @Query("""
            select count(*) from (
               select value
                   from expressions
                   where user_id = :userId and distance(:value, value, :maxDistance) != -1
            )
            """)
    public long countForValue(UUID userId, String value, int maxDistance);

    /**
     * Возвращает кол-во всех устойчивых выражений из словаря пользователя с идентификатором userId, где хотя
     * бы один из переводов каждого устойчивого выражения равен значению translate.
     * @param userId идентификатор пользователя, из устойчивых выражений которого делается выборка
     * @param translate один из возможных переводов искомых устойчивых выражений
     * @return кол-во устойчивых выражений, у которых хотя бы один из переводов равен translate
     */
    @Query("""
            select count(*) from expressions
                inner join expressions_translations
                    on expressions.expression_id = expressions_translations.expression_id
                       and expressions.user_id = :userId
                       and expressions_translations.value = :translate;
            """)
    public long countForTranslate(UUID userId, String translate);

    /**
     * Возвращает часть всех устойчивых выражений из словаря пользователя с идентификатором userId. Если в
     * словаре пользователя нет ни одного устойчивого выражения - возвращает пустой список.
     * @param userId идентификатор пользователя, из выражений которого делается выборка
     * @param pageable параметры пагинации и сортировки устойчивых выражений
     * @return страницу устойчивых выражений.
     */
    public Page<Expression> findByUserId(UUID userId, Pageable pageable);

    /**
     * Возвращает устойчивые выражения доступные для повторения в указанную дату с английского языка на родной
     * язык пользователя. Все устойчивые выражения берутся из словаря пользователя с идентификатором userId.
     * Порядок сортировки выражений - лексикографический. Если нет выражений доступных для повторения в текущую
     * дату - возвращает пустой список.
     * @param userId идентификатор пользователя, из устойчивых выражений которого делается выборка
     * @param date дата для которой подбираются доступные для повторения выражения
     * @param limit максимальное кол-во выражений в возвращаемом списке
     * @param offset кол-во пропущенных выражений от начала исходной выборки, которые не будут включены в список
     * @return список устойчивых выражений
     */
    @Query("""
            select * from expressions
             where user_id = :userId and (last_date_of_repeat_from_english + repeat_interval_from_english) <= :date
             order by value limit :limit offset :offset;
            """)
    public List<Expression> findAllForRepeatFromEnglish(UUID userId, LocalDate date, long limit, long offset);

    /**
     * Возвращает устойчивые выражения доступные для повторения в текущую дату с родного языка пользователя на
     * английский язык. Все устойчивые выражения берутся из словаря пользователя с идентификатором userId.
     * Порядок сортировки выборки выражений - лексикографический. Если нет устойчивых выражений доступных для
     * повторения в текущую дату - возвращает пустой список.
     * @param userId идентификатор пользователя, из устойчивых выражений которого делается выборка
     * @param date дата для которой подбираются доступные для повторения выражения
     * @param limit максимальное кол-во выражений в возвращаемом списке
     * @param offset кол-во пропущенных выражений от начала исходной выборки, которые не будут включены в список
     * @return список устойчивых выражений.
     */
    @Query("""
            select * from expressions
             where user_id = :userId and (last_date_of_repeat_from_native + repeat_interval_from_native) <= :date
             order by value limit :limit offset :offset;
            """)
    public List<Expression> findAllForRepeatFromNative(UUID userId, LocalDate date, long limit, long offset);

}
