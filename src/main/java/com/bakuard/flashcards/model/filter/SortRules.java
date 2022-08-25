package com.bakuard.flashcards.model.filter;

import com.bakuard.flashcards.validation.InvalidParameter;
import org.springframework.data.domain.Sort;

import java.util.Arrays;

public class SortRules {

    public SortRules() {

    }

    public Sort toWordsSort(String sortRule) {
        return Arrays.stream(sortRule.split(",")).
                map(p -> {
                    String[] ps = p.split("\\.");
                    return Sort.by(
                            Sort.Direction.fromString(ps[1]),
                            checkWordParameter(ps[0])
                    );
                }).
                reduce(Sort::and).
                orElse(Sort.by("value").ascending());
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

}
