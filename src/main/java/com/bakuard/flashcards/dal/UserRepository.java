package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.dal.fragment.UserSaver;
import com.bakuard.flashcards.model.auth.credential.User;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Отвечает за сохранение, извлечение и удаление учетных данных пользователей из постоянного хранилища.
 */
@Repository
public interface UserRepository extends PagingAndSortingRepository<User, UUID>, UserSaver<User> {

    /**
     * Возвращает пользователя имеющего указанную почту. Если нет пользователя с такой почтой - возвращает
     * пустой Optional.
     * @param email почта искомого пользователя
     * @return пользователя имеющего указанную почту.
     */
    @Query("select * from users where email = :email")
    Optional<User> findByEmail(String email);

    /**
     * Проверяет - существует ли пользователь с указанной почтой.
     * @param email почта искомого пользователя
     * @return true - если условие описанное выше выполняется, иначе - false.
     */
    @Query("select count(*) > 0 as existsColumn from users where email = :email;")
    boolean existsByEmail(String email);

    /**
     * Возвращает кол-во пользователей имеющих указанную роль.
     * @param role общая роль искомых пользователей
     * @return кол-во пользователей имеющих указанную роль.
     */
    @Query("""
            select count(*) as userNumber
                from users
                inner join roles
                    on roles.user_id = users.user_id
                       and roles.name = :role;
            """)
    long countForRole(String role);

    /**
     * Возвращает заданную часть пользователей имеющих указанную роль.
     * @param role общая роль искомых пользователей
     * @param limit максимальное кол-во пользователей в возвращаемом списке
     * @param offset кол-во пропущенных пользователей от начала исходной выборки, которые не будут
     *               включены в список
     * @return список пользователей.
     */
    @Query("""
            select *
                from users
                inner join roles
                    on roles.user_id = users.user_id
                       and roles.name = :role
                order by email limit :limit offset :offset;
            """)
    List<User> findByRole(String role, long limit, long offset);

}
