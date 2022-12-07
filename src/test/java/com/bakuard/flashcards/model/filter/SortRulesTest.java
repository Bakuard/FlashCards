package com.bakuard.flashcards.model.filter;

import com.bakuard.flashcards.config.SpringConfig;
import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.validation.InvalidParameter;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:test.properties")
@Import({SpringConfig.class, TestConfig.class})
class SortRulesTest {

    @Autowired
    private SortRules sortRules;

    @Test
    @DisplayName("""
            getDefaultSort(sortedEntity):
             sortedEntity = null
             => throw exception
            """)
    public void getDefaultSort1() {
        Assertions.assertThatNullPointerException().
                isThrownBy(() -> sortRules.getDefaultSort(null));
    }

    @Test
    @DisplayName("""
            getDefaultSort(sortedEntity):
             sortedEntity = WORD
            """)
    public void getDefaultSort2() {
        Sort actual = sortRules.getDefaultSort(SortedEntity.WORD);

        Assertions.assertThat(actual).
                containsExactly(Sort.Order.asc("value"));
    }

    @Test
    @DisplayName("""
            getDefaultSort(sortedEntity):
             sortedEntity = EXPRESSION
            """)
    public void getDefaultSort3() {
        Sort actual = sortRules.getDefaultSort(SortedEntity.EXPRESSION);

        Assertions.assertThat(actual).
                containsExactly(Sort.Order.asc("value"));
    }

    @Test
    @DisplayName("""
            getDefaultSort(sortedEntity):
             sortedEntity = WORD_STATISTIC
            """)
    public void getDefaultSort4() {
        Sort actual = sortRules.getDefaultSort(SortedEntity.WORD_STATISTIC);

        Assertions.assertThat(actual).
                containsExactly(
                        Sort.Order.asc("remember_from_english"),
                        Sort.Order.asc("value")
                );
    }

    @Test
    @DisplayName("""
            getDefaultSort(sortedEntity):
             sortedEntity = EXPRESSION_STATISTIC
            """)
    public void getDefaultSort5() {
        Sort actual = sortRules.getDefaultSort(SortedEntity.EXPRESSION_STATISTIC);

        Assertions.assertThat(actual).
                containsExactly(
                        Sort.Order.asc("remember_from_english"),
                        Sort.Order.asc("value")
                );
    }

    @Test
    @DisplayName("""
            getDefaultSort(sortedEntity):
             sortedEntity = USER
            """)
    public void getDefaultSort6() {
        Sort actual = sortRules.getDefaultSort(SortedEntity.USER);

        Assertions.assertThat(actual).
                containsExactly(Sort.Order.asc("id"));
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortedEntity is null
             => exception
            """)
    public void toSort1() {
        Assertions.assertThatNullPointerException().
                isThrownBy(() -> sortRules.toSort("value.asc", null));
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortRules is null,
             sortedEntity is WORD
             => return default sort for WORD
            """)
    public void toSort2() {
        Sort actual = sortRules.toSort(null, SortedEntity.WORD);

        Assertions.assertThat(actual).
                containsExactly(Sort.Order.asc("value"));
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortRules is null,
             sortedEntity is EXPRESSION
             => return default sort for EXPRESSION
            """)
    public void toSort3() {
        Sort actual = sortRules.toSort(null, SortedEntity.EXPRESSION);

        Assertions.assertThat(actual).
                containsExactly(Sort.Order.asc("value"));
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortRules is null,
             sortedEntity is WORD_STATISTIC
             => return default sort for WORD_STATISTIC
            """)
    public void toSort4() {
        Sort actual = sortRules.toSort(null, SortedEntity.WORD_STATISTIC);

        Assertions.assertThat(actual).
                containsExactly(
                        Sort.Order.asc("remember_from_english"),
                        Sort.Order.asc("value")
                );
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortRules is null,
             sortedEntity is EXPRESSION_STATISTIC
             => return default sort for EXPRESSION_STATISTIC
            """)
    public void toSort5() {
        Sort actual = sortRules.toSort(null, SortedEntity.EXPRESSION_STATISTIC);

        Assertions.assertThat(actual).
                containsExactly(
                        Sort.Order.asc("remember_from_english"),
                        Sort.Order.asc("value")
                );
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortRules is null,
             sortedEntity is USER
             => return default sort for USER
            """)
    public void toSort6() {
        Sort actual = sortRules.toSort(null, SortedEntity.USER);

        Assertions.assertThat(actual).
                containsExactly(Sort.Order.asc("id"));
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortRules contains only fields with unique constraint,
             sortedEntity is WORD
             => return correct Sort object for WORD
            """)
    public void toSort7() {
        Sort actual = sortRules.toSort("value.asc", SortedEntity.WORD);

        Assertions.assertThat(actual).
                containsExactly(Sort.Order.asc("value"));
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortRules contains only fields with unique constraint,
             sortedEntity is EXPRESSION
             => return correct Sort object for EXPRESSION
            """)
    public void toSort8() {
        Sort actual = sortRules.toSort("value.asc", SortedEntity.EXPRESSION);

        Assertions.assertThat(actual).
                containsExactly(Sort.Order.asc("value"));
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortRules contains only fields with unique constraint,
             sortedEntity is USER
             => return correct Sort object for USER
            """)
    public void toSort9() {
        Sort actual1 = sortRules.toSort("id.asc", SortedEntity.USER);
        Sort actual2 = sortRules.toSort("email.asc", SortedEntity.USER);

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(actual1).
                containsExactly(Sort.Order.asc("id"));
        softAssertions.assertThat(actual2).
                containsExactly(Sort.Order.asc("email"));
        softAssertions.assertAll();
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortRules contains only fields without unique constraints
             sortedEntity is WORD
             => return correct Sort object for WORD with additional unique field
            """)
    public void toSort10() {
        Sort actual = sortRules.toSort(
                "repeat_interval_from_english.asc, last_date_of_repeat_from_native.desc",
                SortedEntity.WORD);

        Assertions.assertThat(actual).
                containsExactly(
                        Sort.Order.asc("repeat_interval_from_english"),
                        Sort.Order.desc("last_date_of_repeat_from_native"),
                        Sort.Order.asc("value")
                );
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortRules contains only fields without unique constraints
             sortedEntity is EXPRESSION
             => return correct Sort object for EXPRESSION with additional unique field
            """)
    public void toSort11() {
        Sort actual = sortRules.toSort(
                "repeat_interval_from_english.asc, last_date_of_repeat_from_native.desc",
                SortedEntity.EXPRESSION);

        Assertions.assertThat(actual).
                containsExactly(
                        Sort.Order.asc("repeat_interval_from_english"),
                        Sort.Order.desc("last_date_of_repeat_from_native"),
                        Sort.Order.asc("value")
                );
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortRule contains some fields without sort directions
             => use ascending sort direction by default
            """)
    public void toSort12() {
        Sort actual = sortRules.toSort(
                "repeat_interval_from_english, repeat_interval_from_native.desc, value",
                SortedEntity.WORD
        );

        Assertions.assertThat(actual).
                containsExactly(
                        Sort.Order.asc("repeat_interval_from_english"),
                        Sort.Order.desc("repeat_interval_from_native"),
                        Sort.Order.asc("value")
                );
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortRules contains white spaces characters
             => ignore white spaces characters and create correct characters
            """)
    public void toSort13() {
        Sort actual = sortRules.toSort(
                """
                        repeat_interval_from_english,
                                   repeat_interval_from_native.desc,
                                   
                                   
                                   value
                        """,
                SortedEntity.WORD
        );

        Assertions.assertThat(actual).
                containsExactly(
                        Sort.Order.asc("repeat_interval_from_english"),
                        Sort.Order.desc("repeat_interval_from_native"),
                        Sort.Order.asc("value")
                );
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortRules is blank
             => return default sort for sortedEntity
            """)
    public void toSort14() {
        Sort actual = sortRules.toSort("     ", SortedEntity.WORD);

        Assertions.assertThat(actual).
                containsExactly(Sort.Order.asc("value"));
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortRules contains unsupported field for WORD,
             sortedEntity is WORD
             => throw exception
            """)
    public void toSort15() {
        Assertions.assertThatExceptionOfType(InvalidParameter.class).
                isThrownBy(
                        () -> sortRules.toSort("value, unknownParameter", SortedEntity.WORD)
                ).extracting(InvalidParameter::getMessageKey, InstanceOfAssertFactories.STRING).
                isEqualTo("SortRules.invalidParameter");
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortRules contains unsupported field for EXPRESSION,
             sortedEntity is EXPRESSION
             => throw exception
            """)
    public void toSort16() {
        Assertions.assertThatExceptionOfType(InvalidParameter.class).
                isThrownBy(
                        () -> sortRules.toSort("value, unknownParameter", SortedEntity.EXPRESSION)
                ).extracting(InvalidParameter::getMessageKey, InstanceOfAssertFactories.STRING).
                isEqualTo("SortRules.invalidParameter");
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortRules contains unsupported field for USER,
             sortedEntity is USER
             => throw exception
            """)
    public void toSort17() {
        Assertions.assertThatExceptionOfType(InvalidParameter.class).
                isThrownBy(
                        () -> sortRules.toSort("email, unknownParameter", SortedEntity.USER)
                ).extracting(InvalidParameter::getMessageKey, InstanceOfAssertFactories.STRING).
                isEqualTo("SortRules.invalidParameter");
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortRules contains unsupported field for WORD_STATISTIC,
             sortedEntity is WORD_STATISTIC
             => throw exception
            """)
    public void toSort18() {
        Assertions.assertThatExceptionOfType(InvalidParameter.class).
                isThrownBy(
                        () -> sortRules.toSort(
                                "remember_from_english, unknownParameter",
                                SortedEntity.WORD_STATISTIC)
                ).extracting(InvalidParameter::getMessageKey, InstanceOfAssertFactories.STRING).
                isEqualTo("SortRules.invalidParameter");
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortRules contains unsupported field for EXPRESSION_STATISTIC,
             sortedEntity is EXPRESSION_STATISTIC
             => throw exception
            """)
    public void toSort19() {
        Assertions.assertThatExceptionOfType(InvalidParameter.class).
                isThrownBy(
                        () -> sortRules.toSort(
                                "remember_from_english, unknownParameter",
                                SortedEntity.EXPRESSION_STATISTIC)
                ).extracting(InvalidParameter::getMessageKey, InstanceOfAssertFactories.STRING).
                isEqualTo("SortRules.invalidParameter");
    }

    @Test
    @DisplayName("""
            toSort(sortRules, sortedEntity):
             sortRules contains unsupported sort direction
             => throw exception
            """)
    public void toSort20() {
        Assertions.assertThatExceptionOfType(InvalidParameter.class).
                isThrownBy(
                        () -> sortRules.toSort(
                                "repeat_interval_from_english.desc, value.unsupportedDirection",
                                SortedEntity.WORD)
                ).extracting(InvalidParameter::getMessageKey, InstanceOfAssertFactories.STRING).
                isEqualTo("SortRules.unknownSortDirection");
    }

}