package dev.chafon.springbootrest.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
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
}
