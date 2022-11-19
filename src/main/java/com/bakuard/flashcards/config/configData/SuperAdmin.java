package com.bakuard.flashcards.config.configData;

public record SuperAdmin(String password,
                         String mail,
                         boolean recreate) {

    public String roleName() {
        return "SUPER_ADMIN";
    }

}
