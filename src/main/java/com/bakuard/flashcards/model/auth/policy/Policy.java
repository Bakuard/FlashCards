package com.bakuard.flashcards.model.auth.policy;

import com.bakuard.flashcards.model.auth.request.AuthRequest;

/**
 * Политика доступа к данным. Содержит набор правил в соответствии с которыми и решается можно ли
 * предоставить доступ пользователю <b>X</b> к ресурсу <b>Y</b> для выполнения действия <b>Z</b>.
 */
@FunctionalInterface
public interface Policy {

    /**
     * Проверяет - можно ли предоставить доступ пользователю <b>X</b> к ресурсу <b>Y</b> для выполнения
     * действия <b>Z</b> (подробнее см. {@link AuthRequest}). Результат возвращается в виде {@link Access}.<br/>
     * <b>ВАЖНО!</b> Метод не должен возвращать null.
     * @param request см. {@link AuthRequest}.
     * @return результат проверки на наличие необходимых прав доступа.
     */
    public Access checkAccess(AuthRequest request);

}
