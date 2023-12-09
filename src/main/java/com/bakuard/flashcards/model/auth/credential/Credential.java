package com.bakuard.flashcards.model.auth.credential;

import com.bakuard.flashcards.validation.annotation.Password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

/**
 * Учетные данные пользователя.
 * @param email почта пользователя. Требование: должна существовать.
 * @param password пароль пользователя в открытом виде. Требования:<br/>
 *                 1. не должен быть null <br/>
 *                 2. должен содержать отображаемые символы <br/>
 *                 3. длина пароля должна принадлежать промежутку [8, 50] <br/>
 */
public record Credential(@NotNull(message = "Credential.email.notNull")
                         @Email(message = "Credential.email.format") String email,
                         @Password(message = "Credential.password.format") String password) {}
