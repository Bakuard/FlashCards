package com.bakuard.flashcards.service;

import com.bakuard.flashcards.dal.UserRepository;
import com.bakuard.flashcards.model.credential.User;
import com.bakuard.flashcards.model.expression.Expression;
import com.bakuard.flashcards.validation.UnknownEntityException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Transactional
public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void tryDeleteById(UUID userId) {
        if(!existsById(userId)) {
            throw new UnknownEntityException(
                    "Unknown user with id=" + userId,
                    "User.unknown");
        }
        userRepository.deleteById(userId);
    }

    public boolean existsById(UUID userId) {
        return userRepository.existsById(userId);
    }

    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public long count() {
        return userRepository.count();
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

}
