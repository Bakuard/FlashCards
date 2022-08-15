package com.bakuard.flashcards.service;

import com.bakuard.flashcards.dal.UserRepository;
import com.bakuard.flashcards.model.credential.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public void deleteById(UUID userId) {
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
