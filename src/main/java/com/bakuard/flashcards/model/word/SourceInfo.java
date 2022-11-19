package com.bakuard.flashcards.model.word;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public record SourceInfo(@NotBlank(message = "SourceInfo.utl.notBlank") String url,
                         @NotBlank(message = "SourceInfo.sourceName.notBlank") String sourceName,
                         @NotNull(message = "SourceInfo.recentUpdateDate.notNull") LocalDate recentUpdateDate) {}
