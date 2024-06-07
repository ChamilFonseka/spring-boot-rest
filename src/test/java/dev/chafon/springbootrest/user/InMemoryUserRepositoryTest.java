package dev.chafon.springbootrest.user;

import dev.chafon.springbootrest.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static dev.chafon.springbootrest.Constants.ID_CANNOT_BE_NULL;
import static dev.chafon.springbootrest.Constants.USERNAME_CANNOT_BE_NULL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryUserRepositoryTest {

    private InMemoryUserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoUsers() {
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void findAll_shouldReturnUsersWhenUsersExist() {
        User john = new User(1, "John Doe", "johnD", "john.doe@mail.com");
        User jean = new User(2, "Jane Doe", "janeD", "jane.doe@mail.com");
        userRepository.save(john);
        userRepository.save(jean);

        List<User> users = userRepository.findAll();

        assertThat(users).hasSize(2);
        assertThat(users).contains(john, jean);
    }

    @Test
    void findById_shouldReturnUserWhenUserExists() {
        User john = new User(1, "John Doe", "johnD", "john.doe@mail.com");
        userRepository.save(john);

        Optional<User> userExpected = userRepository.findById(john.id());
        assertThat(userExpected).isPresent();
        assertThat(userExpected.get()).isEqualTo(john);
    }

    @Test
    void findById_shouldReturnEmptyOptionalWhenUserDoesNotExist() {
        Optional<User> userExpected = userRepository.findById(1);
        assertThat(userExpected).isEmpty();
    }

    @Test
    void findById_shouldThrowNullPointerExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> userRepository.findById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(ID_CANNOT_BE_NULL);
    }

    @Test
    void save_shouldPersistUser() {
        User user = new User(null, "John Doe", "johnD", "john.doe@mail.com");

        User savedUser = userRepository.save(user);
        assertThat(savedUser.id()).isNotNull();

        Optional<User> userExpected = userRepository.findById(savedUser.id());
        assertThat(userExpected).isPresent();
        assertThat(userExpected.get()).isEqualTo(savedUser);
    }

    @Test
    void save_shouldThrowNullPointerExceptionWhenUserIsNull() {
        assertThatThrownBy(() -> userRepository.save(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(Constants.USER_CANNOT_BE_NULL);
    }

    @Test
    void existsBYUser_shouldReturnTrueWhenUserExists() {
        User john = new User(1, "John Doe", "johnD", "john.doe@mail.com");
        userRepository.save(john);

        boolean exists = userRepository.existsByUsername(john.username());
        assertThat(exists).isTrue();
    }

    @Test
    void existsBYUser_shouldReturnFalseWhenUserDoesNotExist() {
        boolean exists = userRepository.existsByUsername("johnD");
        assertThat(exists).isFalse();
    }

    @Test
    void existsByUserName_shouldThrowNullPointerExceptionWhenUsernameIsNull() {
        assertThatThrownBy(() -> userRepository.existsByUsername(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(USERNAME_CANNOT_BE_NULL);
    }

    @Test
    void deleteById_shouldDeleteUser() {
        User john = new User(1, "John Doe", "johnD", "john.doe@mail.com");
        userRepository.save(john);
        userRepository.deleteById(john.id());

        Optional<User> userExpected = userRepository.findById(john.id());
        assertThat(userExpected).isEmpty();
    }

    @Test
    void deleteById_shouldThrowNullPointerExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> userRepository.deleteById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(ID_CANNOT_BE_NULL);
    }

    @Test
    void existsById_shouldReturnTrueWhenUserExists() {
        User john = new User(1, "John Doe", "johnD", "john.doe@mail.com");
        userRepository.save(john);

        boolean exists = userRepository.existsById(john.id());
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_shouldReturnFalseWhenUserDoesNotExist() {
        boolean exists = userRepository.existsById(1);
        assertThat(exists).isFalse();
    }

    @Test
    void existsById_shouldThrowNullPointerExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> userRepository.existsById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(ID_CANNOT_BE_NULL);
    }
}
