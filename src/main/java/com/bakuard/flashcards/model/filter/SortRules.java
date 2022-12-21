package com.bakuard.flashcards.model.filter;

import com.bakuard.flashcards.validation.exception.InvalidParameter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Преобразует правила сортировки заданные в виде строки в объект Sort. Строка задающая правила сортировки
 * имеет вид: fieldA.direction, fieldB.direction, где fieldA и fieldB поля по которым сортируется сущность,
 * а direction - направление сортировки. При этом кол-во полей может быть больше.
 */
public class SortRules {

    public SortRules() {

    }

    /**
     * Возвращает параметры сортировки по умолчанию для заданной сущности.
     * @param sortedEntity перечисление указывающее тип сортируемой сущности.
     * @return параметры сортировки по умолчанию для заданной сущности.
     * @throws NullPointerException если sortedEntity является null.
     */
    public Sort getDefaultSort(SortedEntity sortedEntity) {
        Sort result = null;
        switch(Objects.requireNonNull(sortedEntity, "sortedEntity can't be null")) {
            case EXPRESSION, WORD ->
                    result = Sort.by("value").ascending();
            case EXPRESSION_STATISTIC, WORD_STATISTIC ->
                    result = Sort.by("remember_from_english", "value").ascending();
            case USER ->
                    result = Sort.by("id").ascending();
            default -> throw new IllegalArgumentException("Unsupported sorted entity = '" + sortedEntity +'\'');
        }
        return result;
    }

    /**
     * Преобразует правила сортировки заданные в виде строки в объект Sort и возвращает его. Правила сортировки
     * проверяются для указанного типа сортируемой сущности. Особые случаи: <br/>
     * 1. Если sortRules является null - возвращает правила сортировки по умолчанию. <br/>
     * 2. Если sortRules не содержит ни одного отображаемого символа - возвращает правила сортировки по умолчанию. <br/>
     * 3. Если среди сортируемых полей заданных в sortRules нет ни одного поля, значение которого
     *    является уникальным для указанного типа сущностей <b>И</b> сущность поддерживает сортировку по одному из таких
     *    полей - то такое поле будет добавлено в конец правила с возрастающим порядком сортировки. <br/>
     * 4. Если sortRules содержит пробельные символы, они будут проигнорированы. <br/>
     * @param sortRules правила сортировки заданные в виде строки.
     * @param sortedEntity тип сортируемой сущности.
     * @return правила сортировки в виде объекта Sort.
     * @throws NullPointerException - если sortedEntity является null.
     * @throws InvalidParameter - если указанный параметр сортировки для заданной сущности не поддерживается или
     *                            указан не действительное направление сортировки. {@link InvalidParameter#getMessageKey()}
     *                            вернет SortRules.unknownSortDirection или SortRules.invalidParameter
     */
    public Sort toSort(String sortRules, SortedEntity sortedEntity) {
        Objects.requireNonNull(sortedEntity, "sortedEntity can't be null");
        return toSortRuleStream(sortRules).
                map(p -> {
                    String[] sr = p.split("\\.");
                    return Sort.by(checkSortDirection(sr), checkParameter(sr, sortedEntity));
                }).
                reduce(Sort::and).
                map(sort -> addAdditionalParameter(sort, sortedEntity)).
                orElse(getDefaultSort(sortedEntity));
    }


    private Sort.Direction checkSortDirection(String[] preparedSortRule) {
        String sortDirection = preparedSortRule.length > 1 ? preparedSortRule[1] : "asc";
        final String processedSortDirection = StringUtils.normalizeSpace(sortDirection).toUpperCase();
        Sort.Direction result = null;

        switch(processedSortDirection) {
            case "ASC", "ASCENDING" -> result = Sort.Direction.ASC;
            case "DESC", "DESCENDING" -> result = Sort.Direction.DESC;
            default -> throw new InvalidParameter("Unsupported sort direction '" + sortDirection + '\'',
                    "SortRules.unknownSortDirection");
        }

        return result;
    }

    private String checkParameter(String[] preparedSortRule, SortedEntity sortedEntity) {
        final String processedParameter = StringUtils.normalizeSpace(preparedSortRule[0]);

        switch(sortedEntity) {
            case EXPRESSION, WORD -> assertParameterIsOneOf(processedParameter,
                    "value",
                    "repeat_interval_from_english",
                    "last_date_of_repeat_from_english",
                    "repeat_interval_from_native",
                    "last_date_of_repeat_from_native");
            case USER -> assertParameterIsOneOf(processedParameter,
                    "id", "email");
            case WORD_STATISTIC, EXPRESSION_STATISTIC -> assertParameterIsOneOf(processedParameter,
                    "value",
                    "remember_from_english",
                    "remember_from_native",
                    "not_remember_from_english",
                    "not_remember_from_native");
            default -> throw new IllegalArgumentException("Unsupported sorted entity '" + sortedEntity + '\'');
        }
        return processedParameter;
    }

    private void assertParameterIsOneOf(String parameter, String... params) {
        if(Arrays.stream(params).noneMatch(p -> p.equalsIgnoreCase(parameter))) {
            throw new InvalidParameter("Invalid sort parameter '" + parameter +'\'',
                    "SortRules.invalidParameter");
        }
    }


    private Stream<String> toSortRuleStream(String sortRules) {
        return sortRules == null || sortRules.isBlank() ?
                Stream.empty() :
                Arrays.stream(sortRules.split(",")).map(String::trim);
    }


    private Sort addAdditionalParameter(Sort sort, SortedEntity sortedEntity) {
        Sort result = sort;
        switch(sortedEntity) {
            case WORD, EXPRESSION, WORD_STATISTIC, EXPRESSION_STATISTIC -> {
                if(notContainsParam(sort, "value")) {
                    result = sort.and(Sort.by("value").ascending());
                }
            }
            case USER -> {
                if(notContainsParam(sort, "id", "email")) {
                    result = sort.and(Sort.by("id").ascending());
                }
            }
        }
        return result;
    }

    private boolean notContainsParam(Sort sort, String... params) {
        return Arrays.stream(params).
                noneMatch(param ->
                        sort.stream().anyMatch(order -> order.getProperty().equalsIgnoreCase(param))
                );
    }

}
