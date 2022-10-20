package com.bakuard.flashcards.model.filter;

import com.bakuard.flashcards.validation.InvalidParameter;
import org.springframework.data.domain.Sort;

import java.util.Arrays;

public class SortRules {

    public SortRules() {

    }

    public Sort toSort(String sortRule, SortedEntity sortedEntity) {
        return Arrays.stream(sortRule.split(",")).
                map(p -> {
                    String[] ps = p.split("\\.");
                    return Sort.by(
                            Sort.Direction.fromString(ps[1]),
                            checkParameter(ps[0], sortedEntity)
                    );
                }).
                reduce(Sort::and).
                orElse(Sort.by("value").ascending());
    }


    private String checkParameter(String parameter, SortedEntity sortedEntity) {
        switch(sortedEntity) {
            case EXPRESSION, WORD -> assertParameterIsOneOf(parameter,
                    "value",
                    "repeat_interval_from_english",
                    "last_date_of_repeat_from_english",
                    "repeat_interval_from_native",
                    "last_date_of_repeat_from_native");
            case USER -> assertParameterIsOneOf(parameter,
                    "user_id", "email");
            case WORD_STATISTIC, EXPRESSION_STATISTIC -> assertParameterIsOneOf(parameter,
                    "rememberFromEnglish",
                    "rememberFromNative",
                    "notRememberFromEnglish",
                    "notRememberFromNative");
            default -> throw new IllegalArgumentException("Unsupported sorted entity = " + sortedEntity);
        }
        return parameter;
    }

    private void assertParameterIsOneOf(String parameter, String... values) {
        if(Arrays.stream(values).anyMatch(p -> p.equalsIgnoreCase(parameter))) {
            throw new InvalidParameter("Invalid sort parameter = " + parameter);
        }
    }

}
