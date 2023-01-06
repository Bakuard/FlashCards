package com.bakuard.flashcards.model.auth.policy;

/**
 * Результат проверки прав доступа для каждого конкретного запроса.
 */
public enum Access {

    /**
     * Доступ разрешен.
     */
    ACCEPT(1),
    /**
     * Результат проверки прав доступа не определен. Указывает на ситуацию, когда не удалось найти
     * ни одной политики доступа однозначно определяющей - разрешен ли доступ.
     */
    UNKNOWN(0),
    /**
     * Доступ запрещен.
     */
    DENY(-1);


    private final int level;

    private Access(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

}
