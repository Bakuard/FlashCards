package com.bakuard.flashcards.model.filter;

import com.bakuard.flashcards.validation.InvalidParameter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class SortRules {

    public SortRules() {

    }

    public Sort getDefaultSort(SortedEntity sortedEntity) {
        Sort result = null;
        switch(sortedEntity) {
            case EXPRESSION, WORD ->
                    result = Sort.by("value").ascending();
            case EXPRESSION_STATISTIC, WORD_STATISTIC ->
                    result = Sort.by("remember_from_english").ascending();
            case USER ->
                    result = Sort.by("user_id").ascending();
            default -> throw new IllegalArgumentException("Unsupported sorted entity = " + sortedEntity);
        }
        return getAdditionalParameter(sortedEntity).
                map(result::and).
                orElse(result);
    }

    public Sort toSort(String sortRule, SortedEntity sortedEntity) {
        return toSortRulesStream(sortRule).
                map(p -> {
                    String[] ps = p.split("\\.");
                    return Sort.by(
                            checkSortDirection(ps[1]),
                            checkParameter(ps[0], sortedEntity)
                    );
                }).
                reduce(Sort::and).
                map(sort -> getAdditionalParameter(sortedEntity).
                        map(sort::and).
                        orElse(sort)).
                orElse(getDefaultSort(sortedEntity));
    }


    private Optional<Sort> getAdditionalParameter(SortedEntity sortedEntity) {
        Sort additionalParameter = null;
        switch(sortedEntity) {
            case WORD_STATISTIC,
                    EXPRESSION_STATISTIC,
                    WORD,
                    EXPRESSION -> additionalParameter = Sort.by("value").ascending();
            case USER -> Sort.by("user_id").ascending();
        }
        return Optional.ofNullable(additionalParameter);
    }

    private Sort.Direction checkSortDirection(String sortDirection) {
        final String processedSortDirection = StringUtils.normalizeSpace(sortDirection).toUpperCase();
        Sort.Direction result = null;

        switch(processedSortDirection) {
            case "ASC", "ASCENDING" -> result = Sort.Direction.ASC;
            case "DESC", "DESCENDING" -> result = Sort.Direction.DESC;
            default -> throw new InvalidParameter("Unsupported sort direction = " + sortDirection,
                    "SortRules.unknownSortDirection");
        }

        return result;
    }

    private String checkParameter(String parameter, SortedEntity sortedEntity) {
        final String processedParameter = StringUtils.normalizeSpace(parameter);

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
                    "remember_from_english",
                    "remember_from_native",
                    "not_remember_from_english",
                    "not_remember_from_native");
            default -> throw new InvalidParameter("Unsupported sorted entity = " + sortedEntity,
                    "SortRules.unknownSortEntity");
        }
        return processedParameter;
    }

    private void assertParameterIsOneOf(String parameter, String... values) {
        if(Arrays.stream(values).noneMatch(p -> p.equalsIgnoreCase(parameter))) {
            throw new InvalidParameter("Invalid sort parameter = " + parameter,
                    "SortRules.invalidParameter");
        }
    }

    private Stream<String> toSortRulesStream(String sortRule) {
        return sortRule == null ? Stream.empty() : Arrays.stream(sortRule.split(","));
    }

}
