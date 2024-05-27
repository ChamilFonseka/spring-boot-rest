package dev.chafon.springbootrest.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static dev.chafon.springbootrest.user.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerMvcTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String API_PATH = "/api/v1/users";

    @Test
    void shouldReturnEmptyUserListAnd200WhenNoUser() throws Exception {
        mvc.perform(get(API_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturnUserListAnd200WhenUsers() throws Exception {
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
    void shouldReturnUserWith200WhenUserExists() throws Exception {
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
    void shouldReturn404WhenUserDoesNotExist() throws Exception {
        int idToGet = 100;
        willThrow(new UserNotFoundException(idToGet))
                .given(userService).getUser(idToGet);

        mvc.perform(get(API_PATH + "/{id}", idToGet))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail",
                        equalTo(USER_NOT_FOUND_EXCEPTION_MESSAGE + idToGet)));

    }

    @Test
    void shouldCreateUserThenReturnCreatedUserAndLocationWith201() throws Exception {
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
    void shouldNotCreateUserWhenUserToCreateIsInvalidThenReturn400() throws Exception {
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
    void shouldNotCreateUserWhenUserAlreadyExistsThenReturn409() throws Exception {
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
    void shouldUpdateUserThenReturn202() throws Exception {
        User userToUpdate = new User(1, "John Doe", "johnD", "john.doe@mail.com");

        mvc.perform(put(API_PATH + "/{id}", userToUpdate.id())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldNotUpdateUserWhenUserToUpdateIsInvalidThenReturn400() throws Exception {
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
    void shouldNotUpdateUserWhenUserToUpdateDoesNotExistThenReturn404() throws Exception {
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
    void shouldDeleteUserThenReturn204() throws Exception {
        mvc.perform(delete(API_PATH + "/{id}", 1))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingUserNotExists() throws Exception {
        int idToDelete = 100;
        willThrow(new UserNotFoundException(idToDelete))
                .given(userService).deleteUser(idToDelete);

        mvc.perform(delete(API_PATH + "/{id}", idToDelete))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", containsString(USER_NOT_FOUND_EXCEPTION_MESSAGE + idToDelete)));
    }
}
