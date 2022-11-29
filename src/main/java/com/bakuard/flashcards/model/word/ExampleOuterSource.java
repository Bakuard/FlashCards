package com.bakuard.flashcards.model.word;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public record ExampleOuterSource(@NotBlank(message = "ExampleOuterSource.utl.notBlank") String url,
                                 @NotBlank(message = "ExampleOuterSource.sourceName.notBlank") String sourceName,
                                 @NotNull(message = "ExampleOuterSource.recentUpdateDate.notNull") LocalDate recentUpdateDate,
                                 @NotBlank(message = "ExampleOuterSource.translate.notBlank") String translate) {}
