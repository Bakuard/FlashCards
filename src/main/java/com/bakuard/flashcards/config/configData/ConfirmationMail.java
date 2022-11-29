package com.bakuard.flashcards.config.configData;

public record ConfirmationMail(String registration,
                               String restorePass,
                               String deletion,
                               String returnAddress) {}
