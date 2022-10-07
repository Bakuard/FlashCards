package com.bakuard.flashcards.model.auth.jwsBody;

import java.util.List;
import java.util.UUID;

public record JwsBody(UUID tokenId, UUID userId, List<Permission> permissions) {}
