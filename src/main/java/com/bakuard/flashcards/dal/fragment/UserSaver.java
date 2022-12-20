package com.bakuard.flashcards.dal.fragment;

import com.bakuard.flashcards.validation.NotUniqueEntityException;

/**
 * Добавляет дополнительное поведение к операции сохранения учетных данных пользователя.
 */
public interface UserSaver<T> {

    /**
     * Сохраняет учетные данные пользователя предварительно проверяя следующие условия: <br/>
     * 1. в постоянном хранилище имеется пользователь с ролью супер-администратора. <br/>
     * 2. идентификатор сохраняемого пользователя отличается от идентификатора супер-администратора. <br/>
     * 3. сохраняемый пользователь имеет роль супер-администратора. <br/>
     * Если выполняются все три условия описанные выше, то пользователь не будет сохранен и будет выброшено
     * исключение.
     * @throws NotUniqueEntityException если выполняется хотя-бы одно из следующих условий:<br/>
     *                                  1. если данный пользователь имеет роль супер-администратора и в постоянном
     *                                     хранилище уже есть другой пользователь с такой же ролью.
     *                                     {@link NotUniqueEntityException#getMessageKey()} вернет User.superAdmin.unique<br/>
     *                                  2. если в постоянном хранилище уже есть другой пользователь с такой почтой.
     *                                     {@link NotUniqueEntityException#getMessageKey()} вернет User.email.unique<br/>
     * @see <a href="https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/CrudRepository.html#save(S)">Документация к CrudRepository#save(entity)</a>
     */
    public T save(T user);

}
