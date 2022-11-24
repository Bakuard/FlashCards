package com.bakuard.flashcards.model.word;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public record OuterSource(@NotBlank(message = "OuterSource.utl.notBlank") String url,
                          @NotBlank(message = "OuterSource.sourceName.notBlank") String sourceName,
                          @NotNull(message = "OuterSource.recentUpdateDate.notNull") LocalDate recentUpdateDate) {}
