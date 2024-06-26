package dev.chafon.springbootrest.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.chafon.springbootrest.post.Post;
import dev.chafon.springbootrest.post.PostNotFoundException;
import dev.chafon.springbootrest.post.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static dev.chafon.springbootrest.Constants.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @MockBean
    private UserService userService;

    @MockBean
    private PostService postService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String API_PATH = "/api/v1/users";

    @Test
    void shouldReturnEmptyListAndStatusOkWhenNoUsers() throws Exception {
        mvc.perform(get(API_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturnUsersAndStatusOkWhenUsersExist() throws Exception {
        List<User> users = List.of(
                new User(1, "John Doe", "johnD", "john.doe@mail.com"),
                new User(2, "Jane Doe", "janeD", "jane.doe@mail.com")
        );
        given(userService.getUsers())
                .willReturn(users);

        mvc.perform(get(API_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", equalTo(users.getFirst().id())))
                .andExpect(jsonPath("$[0].name", equalTo(users.getFirst().name())))
                .andExpect(jsonPath("$[0].username", equalTo(users.getFirst().username())))
                .andExpect(jsonPath("$[0].email", equalTo(users.getFirst().email())))
                .andExpect(jsonPath("$[1].id", equalTo(users.get(1).id())))
                .andExpect(jsonPath("$[1].name", equalTo(users.get(1).name())))
                .andExpect(jsonPath("$[1].username", equalTo(users.get(1).username())))
                .andExpect(jsonPath("$[1].email", equalTo(users.get(1).email())));
    }

    @Test
    void shouldReturnUserAndStatusOkWhenUserExists() throws Exception {
        User user = new User(1, "John Doe", "johnD", "john.doe@mail.com");
        given(userService.getUser(user.id()))
                .willReturn(user);

        mvc.perform(get(API_PATH + "/{id}", user.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(user.id())))
                .andExpect(jsonPath("$.name", equalTo(user.name())))
                .andExpect(jsonPath("$.username", equalTo(user.username())))
                .andExpect(jsonPath("$.email", equalTo(user.email())));
    }

    @Test
    void shouldReturnStatusNotFoundWhenUserDoesNotExist() throws Exception {
        Integer idToGet = 100;
        willThrow(new UserNotFoundException(idToGet))
                .given(userService).getUser(idToGet);

        mvc.perform(get(API_PATH + "/{id}", idToGet))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail",
                        equalTo(USER_NOT_FOUND_EXCEPTION_MESSAGE + idToGet)));

    }

    @Test
    void shouldCreateUserAndReturnUserAndLocationAndStatusCreated() throws Exception {
        User userToCreate = new User(null, "John Doe", "johnD", "john.doe@mail.com");
        User userCreated = new User(123, "John Doe", "johnD", "john.doe@mail.com");

        given(userService.createUser(userToCreate))
                .willReturn(userCreated);

        mvc.perform(post(API_PATH)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", equalTo(userCreated.id())))
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "http://localhost" + API_PATH + "/" + userCreated.id()));
    }

    @Test
    void shouldReturnStatusBadRequestWhenUserToCreateIsInvalid() throws Exception {
        User userToCreate = new User(null, null, null, null);

        mvc.perform(post(API_PATH)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString(NAME_CANNOT_BE_BLANK)))
                .andExpect(jsonPath("$.detail", containsString(USERNAME_CANNOT_BE_BLANK)))
                .andExpect(jsonPath("$.detail", containsString(EMAIL_CANNOT_BE_BLANK)));
    }

    @Test
    void shouldReturnStatusConflictWhenUserAlreadyExists() throws Exception {
        User userToCreate = new User(null, "John Doe", "johnD", "john.doe@mail.com");

        willThrow(new UserAlreadyExistsException(userToCreate.username()))
                .given(userService).createUser(userToCreate);

        mvc.perform(post(API_PATH)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString(USER_ALREADY_EXISTS_EXCEPTION_MESSAGE + userToCreate.username())));
    }

    @Test
    void shouldUpdateUserAndReturnStatusNoContent() throws Exception {
        User userToUpdate = new User(1, "John Doe", "johnD", "john.doe@mail.com");

        mvc.perform(put(API_PATH + "/{id}", userToUpdate.id())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnStatusBadRequestWhenUpdatingUserIsInvalid() throws Exception {
        User userToUpdate = new User(1, null, null, null);

        mvc.perform(put(API_PATH + "/{id}", userToUpdate.id())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString(NAME_CANNOT_BE_BLANK)))
                .andExpect(jsonPath("$.detail", containsString(USERNAME_CANNOT_BE_BLANK)))
                .andExpect(jsonPath("$.detail", containsString(EMAIL_CANNOT_BE_BLANK)));
    }

    @Test
    void shouldReturnStatusNotFoundWhenUpdatingUserDoesNotExists() throws Exception {
        User userToUpdate = new User(100, "John Doe", "johnD", "john.doe@mail.com");

        willThrow(new UserNotFoundException(userToUpdate.id()))
                .given(userService).updateUser(userToUpdate.id(), userToUpdate);

        mvc.perform(put(API_PATH + "/{id}", userToUpdate.id())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", containsString(USER_NOT_FOUND_EXCEPTION_MESSAGE + userToUpdate.id())));
    }

    @Test
    void shouldDeleteUserAndReturnStatusNoContent() throws Exception {
        mvc.perform(delete(API_PATH + "/{id}", 1))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnStatusNotFoundWhenDeletingUserNotExists() throws Exception {
        Integer idToDelete = 100;
        willThrow(new UserNotFoundException(idToDelete))
                .given(userService).deleteUser(idToDelete);

        mvc.perform(delete(API_PATH + "/{id}", idToDelete))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", containsString(USER_NOT_FOUND_EXCEPTION_MESSAGE + idToDelete)));
    }

    @Test
    void shouldReturnPostsForUserAndStatusOkWhenUserExists() throws Exception {
        Integer userId = 123;
        List<Post> posts = List.of(
                new Post(1, 123, "My first post", "My first post content"),
                new Post(2, 123, "My second post", "My second post content")
        );
        given(userService.getUserPosts(userId))
                .willReturn(posts);

        mvc.perform(get(API_PATH + "/{id}/posts", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", equalTo(posts.getFirst().id())))
                .andExpect(jsonPath("$[0].userId", equalTo(posts.getFirst().userId())))
                .andExpect(jsonPath("$[0].title", equalTo(posts.getFirst().title())))
                .andExpect(jsonPath("$[0].body", equalTo(posts.getFirst().body())))
                .andExpect(jsonPath("$[1].id", equalTo(posts.get(1).id())))
                .andExpect(jsonPath("$[1].userId", equalTo(posts.get(1).userId())))
                .andExpect(jsonPath("$[1].title", equalTo(posts.get(1).title())))
                .andExpect(jsonPath("$[1].body", equalTo(posts.get(1).body())));
    }

    @Test
    void shouldReturnStatusNotFoundWhenUserForPostsDoesNotExist() throws Exception {
        Integer userId = 100;
        willThrow(new UserNotFoundException(userId))
                .given(userService).getUserPosts(userId);

        mvc.perform(get(API_PATH + "/{id}/posts", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", containsString(USER_NOT_FOUND_EXCEPTION_MESSAGE + userId)));

    }

    @Test
    void shouldReturnThePostForUserAndStatusOkWhenUserExists() throws Exception {
        Integer userId = 123;
        Integer postId = 1;
        Post post = new Post(1, 123, "My first post", "My first post content");
        given(userService.getUserPost(userId, postId))
                .willReturn(post);

        mvc.perform(get(API_PATH + "/{id}/posts/{postId}", userId, postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(post.id())))
                .andExpect(jsonPath("$.userId", equalTo(post.userId())))
                .andExpect(jsonPath("$.title", equalTo(post.title())))
                .andExpect(jsonPath("$.body", equalTo(post.body())));
    }

    @Test
    void shouldReturnStatusNotFoundWhenUserForPostDoesNotExist() throws Exception {
        Integer userId = 123;
        Integer postId = 1;
        willThrow(new UserNotFoundException(userId))
                .given(userService).getUserPost(userId, postId);

        mvc.perform(get(API_PATH + "/{id}/posts/{postId}", userId, postId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", containsString(USER_NOT_FOUND_EXCEPTION_MESSAGE + userId)));
    }

    @Test
    void shouldReturnStatusNotFoundWhenPostForUserDoesNotExist() throws Exception {
        Integer userId = 123;
        Integer postId = 100;
        willThrow(new PostNotFoundException(postId))
                .given(userService).getUserPost(userId, postId);

        mvc.perform(get(API_PATH + "/{id}/posts/{postId}", userId, postId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", containsString(POST_NOT_FOUND_EXCEPTION_MESSAGE + postId)));
    }
}
