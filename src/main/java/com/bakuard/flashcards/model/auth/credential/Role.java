package com.bakuard.flashcards.model.auth.credential;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Роль пользователя.
 * @param name наименование роли пользователя.
 */
@Table("roles")
public record Role(@Column("name") @NotBlank(message = "Role.name.notBlank") String name) {}
