package com.bakuard.flashcards.model.auth.credential;

import com.bakuard.flashcards.validation.Password;

import javax.validation.constraints.NotNull;

public record PasswordChangeData(@NotNull(message = "CurrentPassword.notNull") String currentPassword,
                                 @Password(message = "NewPassword.format") String newPassword) {}
