package com.bakuard.flashcards.model.filter;

import com.bakuard.flashcards.validation.InvalidParameter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class SortRules {

    public SortRules() {

    }

    public Sort getDefaultSort(SortedEntity sortedEntity) {
        Sort result = null;
        switch(Objects.requireNonNull(sortedEntity, "sortedEntity can't be null")) {
            case EXPRESSION, WORD ->
                    result = Sort.by("value").ascending();
            case EXPRESSION_STATISTIC, WORD_STATISTIC ->
                    result = Sort.by("remember_from_english", "value").ascending();
            case USER ->
                    result = Sort.by("user_id").ascending();
            default -> throw new IllegalArgumentException("Unsupported sorted entity = '" + sortedEntity +'\'');
        }
        return result;
    }

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
                    "user_id", "email");
            case WORD_STATISTIC, EXPRESSION_STATISTIC -> assertParameterIsOneOf(processedParameter,
                    "value",
                    "remember_from_english",
                    "remember_from_native",
                    "not_remember_from_english",
                    "not_remember_from_native");
            default -> throw new InvalidParameter("Unsupported sorted entity '" + sortedEntity + '\'',
                    "SortRules.unknownSortEntity");
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
                if(notContainsParam(sort, "user_id", "email")) {
                    result = sort.and(Sort.by("user_id").ascending());
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
