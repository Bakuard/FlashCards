package com.bakuard.flashcards.service;

import com.bakuard.flashcards.config.ConfigData;
import com.bakuard.flashcards.dal.UserRepository;
import com.bakuard.flashcards.model.auth.JwsWithUser;
import com.bakuard.flashcards.model.auth.credential.Credential;
import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.validation.UnknownEntityException;
import com.bakuard.flashcards.validation.ValidatorUtil;
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
    private ValidatorUtil validator;

    public AuthService(UserRepository userRepository,
                       JwsService jwsService,
                       EmailService emailService,
                       ConfigData configData,
                       ValidatorUtil validator) {
        this.userRepository = userRepository;
        this.jwsService = jwsService;
        this.emailService = emailService;
        this.configData = configData;
        this.validator = validator;
    }

    @Transactional
    public JwsWithUser enter(Credential credential) {
        validator.assertValid(credential);
        User user = tryFindByEmail(credential.email());
        user.assertCurrentPassword(credential.password());
        String jws = jwsService.generateJws(user.getId(), "common");
        return new JwsWithUser(user, jws);
    }

    @Transactional(readOnly = true)
    public void registerFirstStep(Credential credential) {
        validator.assertValid(credential);
        String jws = jwsService.generateJws(credential, "register");
        emailService.confirmEmailForRegistration(jws, credential.email());
    }

    @Transactional
    public JwsWithUser registerFinalStep(Credential jwsBody) {
        User user = save(User.newBuilder(validator).
                setCredential(jwsBody).
                build());
        String jws = jwsService.generateJws(user.getId(), "register");
        return new JwsWithUser(user, jws);
    }

    @Transactional(readOnly = true)
    public void restorePasswordFirstStep(Credential credential) {
        validator.assertValid(credential);
        String jws = jwsService.generateJws(credential, "restorePassword");
        emailService.confirmEmailForRestorePass(jws, credential.email());
    }

    @Transactional
    public JwsWithUser restorePasswordFinalStep(Credential jwsBody) {
        validator.assertValid(jwsBody);
        User user = tryFindByEmail(jwsBody.email());
        user.setCredential(jwsBody);
        user = save(user);
        String jws = jwsService.generateJws(user.getId(), "restorePassword");
        return new JwsWithUser(user, jws);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public void deletionFirstStep(UUID userId, String email) {
        assertExists(userId);
        assertExists(email);
        String jws = jwsService.generateJws(userId, "deleteUser");
        emailService.confirmEmailForDeletion(jws, email);
    }

    @Transactional
    public void deletionFinalStep(UUID userId) {
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
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public void assertExists(String email) {
        if(!existsByEmail(email)) {
            throw new UnknownEntityException(
                    "Unknown user with email=" + email,
                    "User.unknownEmail"
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
