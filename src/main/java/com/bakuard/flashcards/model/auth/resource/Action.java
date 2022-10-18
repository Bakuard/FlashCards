package com.bakuard.flashcards.model.auth.resource;

import javax.validation.constraints.NotBlank;

public record Action(@NotBlank(message = "Action.name.notBlank") String name) {

    public boolean nameIsOneOf(String... names) {
        boolean result = false;
        for(int i = 0; i < names.length && !result; i++) {
            result = name.equals(names[i]);
        }
        return result;
    }

    public boolean nameIs(String name) {
        return this.name.equals(name);
    }

}
