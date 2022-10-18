package com.bakuard.flashcards.model.auth.policy;

import com.bakuard.flashcards.model.auth.request.AuthRequest;

@FunctionalInterface
public interface Policy {

    public Access checkAccess(AuthRequest request);

}
