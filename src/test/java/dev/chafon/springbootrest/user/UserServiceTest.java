package dev.chafon.springbootrest.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static dev.chafon.springbootrest.user.Constants.USER_NOT_FOUND_EXCEPTION_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class UserServiceTest {
    @Mock
    UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnUsers() {
        given(userRepository.findAll()).willReturn(List.of(
                new User(1, "John Doe", "johnD", "john.doe@mail.com"),
                new User(2, "Jane Doe", "janeD", "jane.doe@mail.com")
        ));

        List<User> users = userService.getUsers();

        assertThat(users).hasSize(2);

        assertThat(users.getFirst().name()).isEqualTo("John Doe");
        assertThat(users.getFirst().email()).isEqualTo("john.doe@mail.com");

        assertThat(users.get(1).name()).isEqualTo("Jane Doe");
        assertThat(users.get(1).email()).isEqualTo("jane.doe@mail.com");

        verify(userRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() {
        given(userRepository.findAll()).willReturn(List.of());

        List<User> users = userService.getUsers();

        assertThat(users).isEmpty();

        verify(userRepository).findAll();
    }

    @Test
    void shouldReturnUserWithGivenId() {
        User user = new User(1, "John Doe", "johnD", "john.doe@mail.com");
        given(userRepository.findById(user.id())).willReturn(Optional.of(user));

        User foundUser = userService.getUser(user.id());

        assertThat(foundUser).isEqualTo(user);

        verify(userRepository).findById(user.id());
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserWithGivenIdDoesNotExist() {
        Integer userId = 1;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(USER_NOT_FOUND_EXCEPTION_MESSAGE + userId);

        verify(userRepository).findById(userId);
    }
}
