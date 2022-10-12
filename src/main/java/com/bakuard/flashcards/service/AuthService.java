package com.bakuard.flashcards.service;

import com.bakuard.flashcards.config.ConfigData;
import com.bakuard.flashcards.dal.UserRepository;
import com.bakuard.flashcards.model.auth.credential.Credential;
import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.validation.UnknownEntityException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public class AuthService {

    private UserRepository userRepository;
    private JwsService jwsService;
    private EmailService emailService;
    private ConfigData configData;

    public AuthService(UserRepository userRepository,
                       JwsService jwsService,
                       EmailService emailService,
                       ConfigData configData) {
        this.userRepository = userRepository;
        this.jwsService = jwsService;
        this.emailService = emailService;
        this.configData = configData;
    }

    public String enter() {
        return null;
    }

    public void registerFirstStep(Credential credential) {

    }

    public String registerFinalStep(Credential jwsBody) {
        return null;
    }

    public void restorePasswordFirstStep(Credential credential) {

    }

    public String restorePasswordFinalStep(Credential jwsBody) {
        return null;
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void tryDeleteById(UUID userId) {
        if(!existsById(userId)) {
            throw new UnknownEntityException(
                    "Unknown user with id=" + userId,
                    "User.unknown");
        }
        userRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public boolean existsById(UUID userId) {
        return userRepository.existsById(userId);
    }

    @Transactional(readOnly = true)
    public void assertExists(UUID userId) {
        if(!existsById(userId)) {
            throw new UnknownEntityException(
                    "Unknown user with id=" + userId,
                    "User.unknownId"
            );
        }
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public User tryFindById(UUID userId) {
        return findById(userId).orElseThrow(
                () -> new UnknownEntityException(
                        "Unknown user with id=" + userId,
                        "User.unknownId"
                )
        );
    }

    @Transactional(readOnly = true)
    public User tryFindByEmail(String email) {
        return findByEmail(email).orElseThrow(
                () -> new UnknownEntityException(
                        "Unknown user with email=" + email,
                        "User.unknownEmail"
                )
        );
    }

    @Transactional(readOnly = true)
    public long count() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

}
