package dev.chafon.springbootrest.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

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
        given(userService.getUsers())
                .willReturn(List.of(
                        new User(1, "John Doe", "johnD", "john.doe@mail.com"),
                        new User(2, "Jane Doe", "janeD", "jane.doe@mail.com")
                ));

        mvc.perform(get(API_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].username").value("johnD"))
                .andExpect(jsonPath("$[0].email").value("john.doe@mail.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Jane Doe"))
                .andExpect(jsonPath("$[1].username").value("janeD"))
                .andExpect(jsonPath("$[1].email").value("jane.doe@mail.com"));
    }

    @Test
    void shouldReturnUserWith200WhenUserExists() throws Exception {
        User user = new User(anyInt(), "John Doe", "johnD", "john.doe@mail.com");
        given(userService.getUser(1))
                .willReturn(user);

        mvc.perform(get(API_PATH + "/{id}", user.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.id()))
                .andExpect(jsonPath("$.name").value(user.name()))
                .andExpect(jsonPath("$.username").value(user.username()))
                .andExpect(jsonPath("$.email").value(user.email()));
    }

    @Test
    void shouldReturn404WhenUserDoesNotExist() throws Exception {
        given(userService.getUser(100))
                .willThrow(new UserNotFoundException("User not found with the id: " + 100));

        mvc.perform(get(API_PATH + "/{id}", 100))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("User not found with the id: " + 100));

    }

    @Test
    void shouldCreateUserThenReturnCreatedUserAndLocationWith201() throws Exception {
        User userToCreate = new User(null, "John Doe", "johnD", "john.doe@mail.com");
        User userCreated = new User(123, "John Doe", "johnD", "john.doe@mail.com");

        given(userService.createUser(userToCreate))
                .willReturn(userCreated);

        MvcResult mvcResult = mvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userCreated.id()))
                .andExpect(header().exists("Location"))
                .andReturn();

        String locationHeader = mvcResult.getResponse().getHeader("Location");

        assertThat(locationHeader).endsWith(API_PATH + "/" + userCreated.id());
    }

    @Test
    void shouldNotCreateUserWhenUserToCreateIsInvalidThenReturn400() throws Exception {
        User userToCreate = new User(null, null, null, null);

        mvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("Name cannot be blank")))
                .andExpect(jsonPath("$.detail", containsString("Username cannot be blank")))
                .andExpect(jsonPath("$.detail", containsString("Email cannot be blank")));
    }

    @Test
    void shouldNotCreateUserWhenUserAlreadyExistsThenReturn409() throws Exception {
        User userToCreate = new User(null, "John Doe", "johnD", "john.doe@mail.com");

        given(userService.createUser(userToCreate))
                .willThrow(new UserAlreadyExistsException("User already exists with the username: " + userToCreate.username()));

        mvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("User already exists with the username: " + userToCreate.username())));
    }

    @Test
    void shouldUpdateUserThenReturn202() throws Exception {
        User userToUpdate = new User(1, "John Doe", "johnD", "john.doe@mail.com");

        mvc.perform(put(API_PATH + "/{id}", userToUpdate.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldNotUpdateUserWhenUserToUpdateIsInvalidThenReturn400() throws Exception {
        User userToUpdate = new User(1, null, null, null);

        mvc.perform(put(API_PATH + "/{id}", userToUpdate.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("Name cannot be blank")))
                .andExpect(jsonPath("$.detail", containsString("Username cannot be blank")))
                .andExpect(jsonPath("$.detail", containsString("Email cannot be blank")));
    }

    @Test
    void shouldNotUpdateUserWhenUserToUpdateDoesNotExistThenReturn404() throws Exception {
        User userToUpdate = new User(100, "John Doe", "johnD", "john.doe@mail.com");

        doThrow(new UserNotFoundException("User not found with the id: " + userToUpdate.id()))
                .when(userService).updateUser(userToUpdate);

        mvc.perform(put(API_PATH + "/{id}", userToUpdate.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", containsString("User not found with the id: " + userToUpdate.id())));
    }

    @Test
    void shouldDeleteUserThenReturn204() throws Exception {
        mvc.perform(delete(API_PATH + "/{id}", 1))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldNotDeleteUserWhenUserDoesNotExistThenReturn404() throws Exception {
        doThrow(new UserNotFoundException("User not found with the id: " + 100))
                .when(userService).deleteUser(100);

        mvc.perform(delete(API_PATH + "/{id}", 100))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", containsString("User not found with the id: " + 100)));
    }
}
