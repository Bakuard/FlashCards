package com.bakuard.flashcards.dal.auth;

import com.bakuard.flashcards.model.auth.credential.User;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, UUID> {

    @Query("select * from users where email = :email")
    Optional<User> findByEmail(String email);

}
