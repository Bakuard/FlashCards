package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.config.SpringConfig;
import com.bakuard.flashcards.config.TestConfig;
import com.bakuard.flashcards.config.configData.ConfigData;
import com.bakuard.flashcards.model.auth.credential.Credential;
import com.bakuard.flashcards.model.auth.credential.User;
import com.bakuard.flashcards.model.filter.SortRules;
import com.bakuard.flashcards.model.filter.SortedEntity;
import com.bakuard.flashcards.validation.ValidatorUtil;
import com.bakuard.flashcards.validation.exception.NotUniqueEntityException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:test.properties")
@Import({SpringConfig.class, TestConfig.class})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DataSourceTransactionManager transactionManager;
    @Autowired
    private ValidatorUtil validator;
    @Autowired
    private ConfigData configData;
    @Autowired
    private SortRules sortRules;

    @BeforeEach
    public void beforeEach() {
        commit(() -> JdbcTestUtils.deleteFromTables(jdbcTemplate,
                "expressions",
                "words",
                "intervals",
                "users",
                "repeat_words_from_english_statistic",
                "repeat_words_from_native_statistic",
                "repeat_expressions_from_english_statistic",
                "repeat_expressions_from_native_statistic",
                "word_outer_source",
                "words_examples_outer_source"
        ));
    }

    @Test
    @DisplayName("""
            save(user):
             saved user has role super_admin,
             user with role super_admin already exists in db
             => exception
            """)
    public void save1() {
        commit(() -> userRepository.save(user(1).addRole(configData.superAdmin().roleName())));
        User user = user(2).addRole(configData.superAdmin().roleName());

        Assertions.assertThatExceptionOfType(NotUniqueEntityException.class).
                isThrownBy(() -> commit(() -> userRepository.save(user))).
                extracting(NotUniqueEntityException::getMessageKey, InstanceOfAssertFactories.type(String.class)).
                isEqualTo("User.superAdmin.unique");
    }

    @Test
    @DisplayName("""
            save(user):
             saved user has role super_admin,
             user with role super_admin not exists in db
             => save user
            """)
    public void save2() {
        User expected = user(1).addRole(configData.superAdmin().roleName());

        commit(() -> userRepository.save(expected));

        Optional<User> actual = userRepository.findById(expected.getId());
        Assertions.assertThat(actual).
                isPresent().get().
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            save(user):
             saved user has role super_admin,
             saved user is super admin,
             user with role super_admin already exists in db
             => save user
            """)
    public void save3() {
        User expected = user(2).addRole(configData.superAdmin().roleName());
        commit(() -> userRepository.save(expected));

        expected.setEmail("newSuperAdminMail@email.com");
        commit(() -> userRepository.save(expected));

        Optional<User> actual = userRepository.findById(expected.getId());
        Assertions.assertThat(actual).
                isPresent().get().
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            save(user):
             there is other user with such email in DB
             => exception
            """)
    public void save4() {
        User user = new User(new Credential(toEmail(1), "password1"));
        commit(() -> userRepository.save(user));
        User userWithDuplicateEmail = new User(new Credential(toEmail(1), "password1"));

        Assertions.assertThatExceptionOfType(NotUniqueEntityException.class).
                isThrownBy(() -> commit(() -> userRepository.save(userWithDuplicateEmail)));
    }

    @Test
    @DisplayName("""
            findByEmail(email):
             there is user with such email
             => return correct user
            """)
    public void findByEmail1() {
        User expected = user(1);
        commit(() -> {
            userRepository.save(expected);
            userRepository.save(user(2));
            userRepository.save(user(3));
        });

        User actual = userRepository.findByEmail(toEmail(1)).orElseThrow();

        Assertions.
                assertThat(expected).
                usingRecursiveComparison().
                isEqualTo(actual);
    }

    @Test
    @DisplayName("""
            findByEmail(email):
             there is not user with such email
             => return empty Optional
            """)
    public void findByEmail2() {
        commit(() -> {
            userRepository.save(user(1));
            userRepository.save(user(2));
            userRepository.save(user(3));
        });

        Optional<User> actual = userRepository.findByEmail(toEmail(1000));

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            existsByEmail(email):
             exists user with such email
             => return true
            """)
    public void existsByEmail1() {
        User expected = user(1);
        commit(() -> {
            userRepository.save(expected);
            userRepository.save(user(2));
            userRepository.save(user(3));
        });

        boolean actual = userRepository.existsByEmail(expected.getEmail());

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("""
            existsByEmail(email):
             not exists user with such email
             => return false
            """)
    public void existsByEmail2() {
        commit(() -> {
            userRepository.save(user(1));
            userRepository.save(user(2));
            userRepository.save(user(3));
        });

        boolean actual = userRepository.existsByEmail(toEmail(1000));

        Assertions.assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("""
            countForRole(role):
             there are not users in database
             => return 0
            """)
    public void countForRole1() {
        long actual = userRepository.countForRole("some role");

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            countForRole(role):
             there are users in database,
             there are not users with this role
             => return 0
            """)
    public void countForRole2() {
        commit(() -> {
            userRepository.save(user(1));
            userRepository.save(user(2));
            userRepository.save(user(3));
        });

        long actual = userRepository.countForRole("some role");

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            countForRole(role):
             there are users in database,
             there are users with this role
             => return count users with this role
            """)
    public void countForRole3() {
        commit(() -> {
            userRepository.save(user(1));
            userRepository.save(user(2).addRole("admin"));
            userRepository.save(user(3).addRole("admin"));
        });

        long actual = userRepository.countForRole("admin");

        Assertions.assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("""
            findByRole(role, limit, offset):
             there are not users in database
             => return empty list
            """)
    public void findByRole1() {
        List<User> actual = userRepository.findByRole("admin", 10, 0);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findByRole(role, limit, offset):
             there are users in database,
             there are not users with this role
             => return empty list
            """)
    public void findByRole2() {
        commit(() -> {
            userRepository.save(user(1));
            userRepository.save(user(2));
            userRepository.save(user(3));
        });

        List<User> actual = userRepository.findByRole("admin", 10, 0);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findByRole(role, limit, offset):
             there are users in database,
             there are users with this role
             => return all user with this role
            """)
    public void findByRole3() {
        List<User> users = List.of(
                user(1),
                user(2).addRole("admin"),
                user(3).addRole("admin")
        );
        commit(() -> users.forEach(user -> userRepository.save(user)));

        List<User> actual = userRepository.findByRole("admin", 10, 0);

        Assertions.assertThat(actual).
                usingRecursiveFieldByFieldElementComparator().
                containsExactly(users.get(1), users.get(2));
    }

    @Test
    @DisplayName("""
            findAll(pageable):
             sorted property is email
             => return correct sorted result
            """)
    public void findAll1() {
        List<User> users = IntStream.range(10, 61).mapToObj(this::user).toList();
        commit(() -> users.forEach(u -> userRepository.save(u)));

        Pageable pageable = PageRequest.of(3, 5, sortRules.toSort("email.desc", SortedEntity.USER));
        Page<User> actual = userRepository.findAll(pageable);

        Assertions.assertThat(actual.getContent()).
                usingRecursiveFieldByFieldElementComparator().
                containsExactly(
                        users.get(35),
                        users.get(34),
                        users.get(33),
                        users.get(32),
                        users.get(31)
                );
    }

    @Test
    @DisplayName("""
            findAll(pageable):
             sorted property is id
             => return correct sorted result
            """)
    public void findAll2() {
        List<User> users = IntStream.range(10, 61).
                mapToObj(this::user).
                collect(Collectors.toCollection(ArrayList::new));
        commit(() -> users.forEach(u -> userRepository.save(u)));
        users.sort(Comparator.comparing(u -> u.getId().toString()));

        Pageable pageable = PageRequest.of(0, 15, sortRules.toSort("id", SortedEntity.USER));
        Page<User> actual = userRepository.findAll(pageable);

        Assertions.assertThat(actual.getContent()).
                usingRecursiveFieldByFieldElementComparator().
                containsExactlyElementsOf(users.subList(0, 15));
    }


    private String toEmail(int number) {
        return "user" + number + "@gmail.com";
    }

    private User user(int number) {
        return new User(new Credential(toEmail(number), "password" + number)).
                setOrGenerateSalt("salt" + number).
                addRole("role1").
                addRole("role2").
                addRole("role3");
    }

    private void commit(Executable executable) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            executable.execute();
            transactionManager.commit(status);
        } catch(RuntimeException e) {
            transactionManager.rollback(status);
            throw e;
        } catch(Throwable e) {
            transactionManager.rollback(status);
            throw new RuntimeException(e);
        }
    }

}