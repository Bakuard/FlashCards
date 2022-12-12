package com.bakuard.flashcards.model.auth.credential;

import com.bakuard.flashcards.validation.Password;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

/**
 * Учетные данные пользователя.
 * @param email почта пользователя.
 * @param password пароль пользователя в открытом виде.
 */
public record Credential(@NotNull(message = "Credential.email.notNull")
                         @Email(message = "Credential.email.format") String email,
                         @Password(message = "Credential.password.format") String password) {}
