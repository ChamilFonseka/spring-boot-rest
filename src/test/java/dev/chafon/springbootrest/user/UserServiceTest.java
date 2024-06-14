package dev.chafon.springbootrest.user;

import dev.chafon.springbootrest.post.Post;
import dev.chafon.springbootrest.post.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static dev.chafon.springbootrest.Constants.USER_ALREADY_EXISTS_EXCEPTION_MESSAGE;
import static dev.chafon.springbootrest.Constants.USER_NOT_FOUND_EXCEPTION_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class UserServiceTest {
    @Mock
    UserRepository userRepository;

    @Mock
    PostService poseService;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> argumentCaptor;

    @Test
    void shouldReturnAllUsers() {
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

    @Test
    void shouldCreateUserAndReturnIt() {
        User userToCreate = new User(null, "John Doe", "johnD", "john.doe@mail.com");

        Integer expectedId = 123;
        given(userRepository.save(userToCreate))
                .willReturn(
                        new User(expectedId, userToCreate.name(), userToCreate.username(), userToCreate.email())
                );

        User userCreated = userService.createUser(userToCreate);

        assertThat(userCreated).isNotNull();
        assertThat(userCreated.id()).isEqualTo(expectedId);

        verify(userRepository).save(argumentCaptor.capture());

        User capturedUser = argumentCaptor.getValue();
        assertThat(capturedUser.id()).isNull();
        assertThat(capturedUser.name()).isEqualTo(userToCreate.name());
        assertThat(capturedUser.username()).isEqualTo(userToCreate.username());
        assertThat(capturedUser.email()).isEqualTo(userToCreate.email());
    }

    @Test
    void shouldThrowUserAlreadyExistsExceptionWhenUpdatingUserExists() {
        User userToCreate = new User(null, "John Dean", "johnD", "john.dean@mail.com");

        given(userRepository.existsByUsername(userToCreate.username()))
                .willReturn(true);

        assertThatThrownBy(() -> userService.createUser(userToCreate))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage(USER_ALREADY_EXISTS_EXCEPTION_MESSAGE + userToCreate.username());

        verify(userRepository).existsByUsername(userToCreate.username());
    }

    @Test
    void shouldUpdateUser() {
        User userToUpdate = new User(1, "Updated name", "johnD", "updated@mail.com");

        given(userRepository.findById(userToUpdate.id()))
                .willReturn(Optional.of(new User(userToUpdate.id(), "Existing name", "johnD", "existing@mail.com")));

        userService.updateUser(userToUpdate.id(), userToUpdate);

        verify(userRepository).findById(userToUpdate.id());
        verify(userRepository).save(argumentCaptor.capture());

        User capturedUser = argumentCaptor.getValue();
        assertThat(capturedUser.id()).isEqualTo(userToUpdate.id());
        assertThat(capturedUser.name()).isEqualTo(userToUpdate.name());
        assertThat(capturedUser.username()).isEqualTo(userToUpdate.username());
        assertThat(capturedUser.email()).isEqualTo(userToUpdate.email());
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUpdatingUserNotExist() {
        User userToUpdate = new User(1, "John Doe", "johnD", "john.doe@mail.com");

        assertThatThrownBy(() -> userService.updateUser(userToUpdate.id(), userToUpdate))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(USER_NOT_FOUND_EXCEPTION_MESSAGE + userToUpdate.id());

        verify(userRepository).findById(userToUpdate.id());
        verify(userRepository, never()).save(userToUpdate);
    }

    @Test
    void shouldNotUpdateTheUsernameAlways() {
        User existingUser = new User(1, "John Doe", "johnD", "john.doe@mail.com");
        User userToUpdate = new User(1, "J D", "johnDoe", "john.doe@abc.com");
        given(userRepository.findById(userToUpdate.id()))
                .willReturn(Optional.of(existingUser));

        userService.updateUser(userToUpdate.id(), userToUpdate);

        verify(userRepository).findById(userToUpdate.id());
        verify(userRepository).save(argumentCaptor.capture());

        User capturedUser = argumentCaptor.getValue();
        assertThat(capturedUser.name()).isEqualTo(userToUpdate.name());
        assertThat(capturedUser.username()).isEqualTo(existingUser.username());
        assertThat(capturedUser.email()).isEqualTo(userToUpdate.email());
    }

    @Test
    void shouldDelete() {
        Integer idToDelete = 1;
        given(userRepository.existsById(idToDelete))
                .willReturn(true);

        userService.deleteUser(idToDelete);

        verify(userRepository).existsById(idToDelete);
        verify(userRepository).deleteById(idToDelete);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenDeletingUserNotExist() {
        Integer idToDelete = 1;
        given(userRepository.existsById(idToDelete))
                .willReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(idToDelete))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(USER_NOT_FOUND_EXCEPTION_MESSAGE + idToDelete);

        verify(userRepository).existsById(idToDelete);
        verify(userRepository, never()).deleteById(idToDelete);
    }

    @Test
    void shouldReturnUserPosts() {
        Integer userId = 123;
        List<Post> expectedPosts = List.of(
                new Post(1, 123, "My first post", "My first post content"),
                new Post(2, 123, "My second post", "My second post content")
        );
        given(userRepository.existsById(userId))
                .willReturn(true);
        given(poseService.getPostsByUser(userId))
                .willReturn(expectedPosts);

        List<Post> userPosts = userService.getUserPosts(userId);

        assertThat(userPosts).isEqualTo(expectedPosts);

        verify(userRepository).existsById(userId);
        verify(poseService).getPostsByUser(userId);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserForPostsNotExist() {
        Integer userId = 123;
        given(userRepository.existsById(userId))
                .willReturn(false);

        assertThatThrownBy(() -> userService.getUserPosts(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(USER_NOT_FOUND_EXCEPTION_MESSAGE + userId);

        verify(userRepository).existsById(userId);
        verify(poseService, never()).getPostsByUser(userId);
    }
}
