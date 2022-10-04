package com.bakuard.flashcards.model.auth.credential;

import com.bakuard.flashcards.validation.Password;

public record PasswordChangeData(@Password(message = "CurrentPassword.format") String currentPassword,
                                 @Password(message = "NewPassword.format") String newPassword) {}
