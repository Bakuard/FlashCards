package com.bakuard.flashcards.model.auth.credential;

import javax.validation.constraints.NotNull;

public record Email(@NotNull(message = "Credential.email.notNull")
                    @javax.validation.constraints.Email(message = "Credential.email.format")
                    String value) {}
