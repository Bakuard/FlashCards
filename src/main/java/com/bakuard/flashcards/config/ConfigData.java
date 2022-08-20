package com.bakuard.flashcards.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("conf")
public record ConfigData(int maxPageSize,
                         String databaseName,
                         String databaseUser,
                         String databasePassword) {}
