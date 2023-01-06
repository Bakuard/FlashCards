package com.bakuard.flashcards.config.configData;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("conf")
public record ConfigData(long jwsLifeTimeInDays,
                         int levenshteinMaxDistance,
                         Database database,
                         Pagination pagination,
                         Smtp smtp,
                         SuperAdmin superAdmin,
                         ConfirmationMail confirmationMail,
                         Jws jws) {}
