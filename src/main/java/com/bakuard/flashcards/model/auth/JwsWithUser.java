package com.bakuard.flashcards.model.auth;

import com.bakuard.flashcards.model.auth.credential.User;

public record JwsWithUser(User user, String jws) {}
