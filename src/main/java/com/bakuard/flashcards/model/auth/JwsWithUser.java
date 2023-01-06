package com.bakuard.flashcards.model.auth;

import com.bakuard.flashcards.model.auth.credential.User;

/**
 * Содержит возвращаемые данные о пользователе и jws этого пользователя.
 * @param user пользователь к которому относится jws.
 * @param jws токен Json Web Security.
 */
public record JwsWithUser(User user, String jws) {}
