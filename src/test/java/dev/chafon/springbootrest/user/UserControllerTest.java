package dev.chafon.springbootrest.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mvc;

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
}
