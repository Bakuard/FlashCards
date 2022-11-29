package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.dal.impl.fragment.UserSaver;
import com.bakuard.flashcards.model.auth.credential.User;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, UUID>, UserSaver<User> {

    @Query("select * from users where email = :email")
    Optional<User> findByEmail(String email);

    @Query("select count(*) > 0 as existsColumn from users where email = :email;")
    boolean existsByEmail(String email);

    @Query("""
            select count(*) as userNumber
                from users
                inner join roles
                    on roles.user_id = users.user_id
                       and roles.name = :role;
            """)
    long countForRole(String role);

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
