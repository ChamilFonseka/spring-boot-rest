package dev.chafon.springbootrest.user;

import dev.chafon.springbootrest.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static dev.chafon.springbootrest.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryUserRepositoryTest {

    private InMemoryUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoUsers() {
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void findAll_shouldReturnUsersWhenUsersExist() {
        User john = repository.save(
                new User(null, "John Doe", "johnD", "john.doe@mail.com"));
        User jean = repository.save(
                new User(null, "Jane Doe", "janeD", "jane.doe@mail.com"));

        List<User> users = repository.findAll();

        assertThat(users).hasSize(2);
        assertThat(users).contains(john, jean);
    }

    @Test
    void findById_shouldReturnUserWhenUserExists() {
        User user = saveATestUser();

        Optional<User> userExpected = repository.findById(user.id());
        assertThat(userExpected).isPresent();
        assertThat(userExpected.get()).isEqualTo(user);
    }

    @Test
    void findById_shouldReturnEmptyOptionalWhenUserDoesNotExist() {
        Optional<User> userExpected = repository.findById(1);
        assertThat(userExpected).isEmpty();
    }

    @Test
    void findById_shouldThrowNullPointerExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> repository.findById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(ID_CANNOT_BE_NULL);
    }

    @Test
    void save_shouldPersistUser() {
        User user = new User(null, "John Doe", "johnD", "john.doe@mail.com");

        User savedUser = repository.save(user);
        assertThat(savedUser.id()).isNotNull();

        Optional<User> userExpected = repository.findById(savedUser.id());
        assertThat(userExpected).isPresent();
        assertThat(userExpected.get()).isEqualTo(savedUser);
    }

    @Test
    void save_shouldThrowNullPointerExceptionWhenUserIsNull() {
        assertThatThrownBy(() -> repository.save(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(Constants.USER_CANNOT_BE_NULL);
    }

    @Test
    void save_shouldThrowIllegalArgumentExceptionWhenUserIdNotExist() {
        User user = new User(1, "John Doe", "johnD", "john.doe@mail.com");

        assertThatThrownBy(() -> repository.save(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(USER_WITH_ID_DOES_NOT_EXIST + " : " + user.id());
    }

    @Test
    void existsBYUser_shouldReturnTrueWhenUserExists() {
        User user = saveATestUser();

        boolean exists = repository.existsByUsername(user.username());
        assertThat(exists).isTrue();
    }

    @Test
    void existsBYUser_shouldReturnFalseWhenUserDoesNotExist() {
        boolean exists = repository.existsByUsername("johnD");
        assertThat(exists).isFalse();
    }

    @Test
    void existsByUserName_shouldThrowNullPointerExceptionWhenUsernameIsNull() {
        assertThatThrownBy(() -> repository.existsByUsername(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(USERNAME_CANNOT_BE_NULL);
    }

    @Test
    void deleteById_shouldDeleteUser() {
        User user = saveATestUser();

        repository.deleteById(user.id());

        Optional<User> userExpected = repository.findById(user.id());
        assertThat(userExpected).isEmpty();
    }

    @Test
    void deleteById_shouldThrowNullPointerExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> repository.deleteById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(ID_CANNOT_BE_NULL);
    }

    @Test
    void existsById_shouldReturnTrueWhenUserExists() {
        User user = saveATestUser();

        boolean exists = repository.existsById(user.id());
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_shouldReturnFalseWhenUserDoesNotExist() {
        boolean exists = repository.existsById(1);
        assertThat(exists).isFalse();
    }

    @Test
    void existsById_shouldThrowNullPointerExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> repository.existsById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(ID_CANNOT_BE_NULL);
    }

    private User saveATestUser() {
        return repository.save(
                new User(null, "John Doe", "johnD", "john.doe@mail.com"));
    }
}
