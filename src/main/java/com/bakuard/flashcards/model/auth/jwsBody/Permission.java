package com.bakuard.flashcards.model.auth.jwsBody;

import java.util.List;
import java.util.UUID;

public record Permission(UUID resourceId, List<String> actions) {}
