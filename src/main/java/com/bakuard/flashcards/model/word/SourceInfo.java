package com.bakuard.flashcards.model.word;

import javax.validation.constraints.NotBlank;

public record SourceInfo(@NotBlank(message = "SourceInfo.utl.notBlank") String url,
                         @NotBlank(message = "SourceInfo.sourceName.notBlank") String sourceName) {}
