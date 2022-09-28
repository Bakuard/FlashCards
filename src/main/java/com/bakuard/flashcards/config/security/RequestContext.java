package com.bakuard.flashcards.config.security;

import java.util.UUID;

public interface RequestContext {

    public UUID getCurrentJwsBody();

}
