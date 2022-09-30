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
            case EXPRESSION -> {
                return checkExpressionParameter(parameter);
            }
            case WORD -> {
                return checkWordParameter(parameter);
            }
            default -> throw new IllegalArgumentException("Unsupported sorted entity = " + sortedEntity);
        }
    }

    private String checkWordParameter(String parameter) {
        if(!"value".equalsIgnoreCase(parameter) &&
                !"repeat_interval".equalsIgnoreCase(parameter) &&
                !"last_date_of_repeat".equalsIgnoreCase(parameter)) {
            throw new InvalidParameter(
                    "Invalid word sort parameter = " + parameter,
                    "Word.invalidSortParameter"
            );
        }

        return parameter;
    }

    private String checkExpressionParameter(String parameter) {
        if(!"value".equalsIgnoreCase(parameter) &&
                !"repeat_interval".equalsIgnoreCase(parameter) &&
                !"last_date_of_repeat".equalsIgnoreCase(parameter)) {
            throw new InvalidParameter(
                    "Invalid expression sort parameter = " + parameter,
                    "Expression.invalidSortParameter"
            );
        }

        return parameter;
    }

}
