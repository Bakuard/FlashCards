package com.bakuard.flashcards.model.auth.resource;

/**
 * Одно из действий которое можно выполнить над ресурсом.
 * @param name имя действия.
 */
public record Action(String name) {

    /**
     * Проверяет - равняется ли имя данного действия над ресурсом одному из указанных.
     * @param names предполагаемые имена данного действия над ресурсом.
     * @return true - если условие описанное выше выполняется, иначе - false.
     */
    public boolean nameIsOneOf(String... names) {
        boolean result = false;
        for(int i = 0; i < names.length && !result; i++) {
            result = name.equals(names[i]);
        }
        return result;
    }

    /**
     * Проверяет - равняется ли имя текущего действия над ресурсом или нет.
     * @param name имя действия над ресурсом.
     * @return true - если условие описанное выше выполняется, иначе - false.
     */
    public boolean nameIs(String name) {
        return this.name.equals(name);
    }

}
