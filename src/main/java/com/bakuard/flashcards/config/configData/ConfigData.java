package com.bakuard.flashcards.config.configData;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("conf")
public record ConfigData(int levenshteinMaxDistance,
                         Database database,
                         Pagination pagination,
                         Smtp smtp,
                         SuperAdmin superAdmin,
                         ConfirmationMail confirmationMail,
                         Jws jws) {}
