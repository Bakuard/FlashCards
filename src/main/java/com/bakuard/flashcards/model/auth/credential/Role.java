package com.bakuard.flashcards.model.auth.credential;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotBlank;

@Table("roles")
public record Role(@Column("name") @NotBlank(message = "Role.name.notBlank") String name) {}
