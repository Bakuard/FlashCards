package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.config.SpringConfig;
import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.config.configData.ConfigData;
import com.bakuard.flashcards.validation.exception.InvalidParameter;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:test.properties")
@Import({SpringConfig.class, TestConfig.class})
class PaginationRequestTest {

    @Autowired
    private ConfigData config;

    @Test
    @DisplayName("""
            toUserPageRequest(pageNumber, pageSize, sortParams, config):
             sortParams is null
             => return Pageable with default sort param for users
            """)
    void toUserPageRequest1() {
        Pageable actual = PaginationRequest.toUserPageRequest(
                0,
                10,
                null,
                config
        );

        Assertions.assertThat(actual).isEqualTo(new PaginationRequest(
                0,
                10,
                Sort.by("id").ascending(),
                config
        ));
    }

    @Test
    @DisplayName("""
            toWordPageRequest(pageNumber, pageSize, sortParams, config):
             sortParams is null
             => return Pageable with default sort param for words
            """)
    void toWordPageRequest1() {
        Pageable actual = PaginationRequest.toWordPageRequest(
                0,
                10,
                null,
                config
        );

        Assertions.assertThat(actual).isEqualTo(new PaginationRequest(
                0,
                10,
                Sort.by("value").ascending(),
                config
        ));
    }

    @Test
    @DisplayName("""
            toWordPageRequest(pageNumber, pageSize, sortParams, config):
             sortParams doesn't contain necessary field
             => return Pageable with sort that contains necessary field
            """)
    void toWordPageRequest2() {
        Pageable actual = PaginationRequest.toWordPageRequest(
                0,
                10,
                "repeat_interval_from_english.asc, last_date_of_repeat_from_native.desc",
                config
        );

        Assertions.assertThat(actual).isEqualTo(new PaginationRequest(
                0,
                10,
                Sort.by(
                        Sort.Order.asc("repeat_interval_from_english"),
                        Sort.Order.desc("last_date_of_repeat_from_native"),
                        Sort.Order.asc("value")
                ),
                config
        ));
    }

    @Test
    @DisplayName("""
            toWordPageRequest(pageNumber, pageSize, sortParams, config):
             sortParams contains only necessary fields
             => return Pageable with sort that contains only that fields
            """)
    void toWordPageRequest3() {
        Pageable actual = PaginationRequest.toWordPageRequest(
                0,
                10,
                "value.desc",
                config
        );

        Assertions.assertThat(actual).isEqualTo(new PaginationRequest(
                0,
                10,
                Sort.by("value").descending(),
                config
        ));
    }

    @Test
    @DisplayName("""
            toWordPageRequest(pageNumber, pageSize, sortParams, config):
             sortParams contains fields without sort direction
             => return Pageable with sort that contains default sort direction for such fields
            """)
    void toWordPageRequest4() {
        Pageable actual = PaginationRequest.toWordPageRequest(
                0,
                10,
                "repeat_interval_from_english, last_date_of_repeat_from_native, value",
                config
        );

        Assertions.assertThat(actual).isEqualTo(new PaginationRequest(
                0,
                10,
                Sort.by(
                        Sort.Order.asc("repeat_interval_from_english"),
                        Sort.Order.asc("last_date_of_repeat_from_native"),
                        Sort.Order.asc("value")
                ),
                config
        ));
    }

    @Test
    @DisplayName("""
            toWordPageRequest(pageNumber, pageSize, sortParams, config):
             sortParams contains white characters
             => return Pageable with sort that ignore white characters
            """)
    void toWordPageRequest5() {
        Pageable actual = PaginationRequest.toWordPageRequest(
                0,
                10,
                """
                        repeat_interval_from_english,
                                   repeat_interval_from_native.desc,
                                   
                                   
                                   value
                        """,
                config
        );

        Assertions.assertThat(actual).isEqualTo(new PaginationRequest(
                0,
                10,
                Sort.by(
                        Sort.Order.asc("repeat_interval_from_english"),
                        Sort.Order.desc("repeat_interval_from_native"),
                        Sort.Order.asc("value")
                ),
                config
        ));
    }

    @Test
    @DisplayName("""
            toWordPageRequest(pageNumber, pageSize, sortParams, config):
             sortParams is blank
             => return Pageable with default sort
            """)
    void toWordPageRequest6() {
        Pageable actual = PaginationRequest.toWordPageRequest(
                0,
                10,
                "       ",
                config
        );

        Assertions.assertThat(actual).isEqualTo(new PaginationRequest(
                0,
                10,
                Sort.by("value").ascending(),
                config
        ));
    }

    @Test
    @DisplayName("""
            toWordPageRequest(pageNumber, pageSize, sortParams, config):
             sortParams contains unsupported sort fields
             => throw Exception
            """)
    void toWordPageRequest7() {
        Assertions.assertThatExceptionOfType(InvalidParameter.class).
                isThrownBy(
                        () -> PaginationRequest.toWordStatisticsPageRequest(
                                0,
                                10,
                                "value, unknownParameter",
                                config
                        )
                ).extracting(InvalidParameter::getMessageKey, InstanceOfAssertFactories.STRING).
                isEqualTo("PaginationRequest.invalidParameter");
    }

    @Test
    @DisplayName("""
            toWordPageRequest(pageNumber, pageSize, sortParams, config):
             sortParams contains unsupported sort direction
             => throw Exception
            """)
    void toWordPageRequest8() {
        Assertions.assertThatExceptionOfType(InvalidParameter.class).
                isThrownBy(
                        () -> PaginationRequest.toWordStatisticsPageRequest(
                                0,
                                10,
                                "value, repeat_interval_from_english.unknownDirection",
                                config
                        )
                ).extracting(InvalidParameter::getMessageKey, InstanceOfAssertFactories.STRING).
                isEqualTo("PaginationRequest.unknownSortDirection");
    }

    @Test
    @DisplayName("""
            toExpressionPageRequest(pageNumber, pageSize, sortParams, config):
             sortParams is null
             => return Pageable with default sort param for expressions
            """)
    void toExpressionPageRequest1() {
        Pageable actual = PaginationRequest.toExpressionPageRequest(
                0,
                10,
                null,
                config
        );

        Assertions.assertThat(actual).isEqualTo(new PaginationRequest(
                0,
                10,
                Sort.by("value").ascending(),
                config
        ));
    }

    @Test
    @DisplayName("""
            toWordStatisticsPageRequest(pageNumber, pageSize, sortParams, config):
             sortParams is null
             => return Pageable with default sort param for expressions
            """)
    void toWordStatisticsPageRequest1() {
        Pageable actual = PaginationRequest.toWordStatisticsPageRequest(
                0,
                10,
                null,
                config
        );

        Assertions.assertThat(actual).isEqualTo(new PaginationRequest(
                0,
                10,
                Sort.by("remember_from_english", "value").ascending(),
                config
        ));
    }

    @Test
    @DisplayName("""
            toExpressionStatisticsPageRequest(pageNumber, pageSize, sortParams, config):
             sortParams is null
             => return Pageable with default sort param for expressions
            """)
    void toExpressionStatisticsPageRequest1() {
        Pageable actual = PaginationRequest.toExpressionStatisticsPageRequest(
                0,
                10,
                null,
                config
        );

        Assertions.assertThat(actual).isEqualTo(new PaginationRequest(
                0,
                10,
                Sort.by("remember_from_english", "value").ascending(),
                config
        ));
    }
}