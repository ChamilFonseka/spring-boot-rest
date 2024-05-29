package dev.chafon.springbootrest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import dev.chafon.springbootrest.user.Constants;
import dev.chafon.springbootrest.user.User;
import dev.chafon.springbootrest.user.UserRepository;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Optional;

import static dev.chafon.springbootrest.user.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ApplicationTests {

    public static final String BASE_URL = "/api/v1/users";
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DirtiesContext
    void shouldReturnUserList() throws Exception {
        User john = new User(1, "John Doe", "johnD", "john.doe@mail.com");
        User jean = new User(2, "Jane Gray", "janeG", "jane.gray@mail.com");
        userRepository.save(john);
        userRepository.save(jean);

        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //Can use DocumentContext
        DocumentContext documentContext = JsonPath.parse(response.getBody());

        int userCount = documentContext.read("$.length()");
        assertThat(userCount).isEqualTo(2);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(1, 2);

        JSONArray names = documentContext.read("$..name");
        assertThat(names).containsExactlyInAnyOrder("John Doe", "Jane Gray");

        int id1 = documentContext.read("[0].id");
        assertThat(id1).isEqualTo(john.id());

        int id2 = documentContext.read("[1].id");
        assertThat(id2).isEqualTo(jean.id());

        String name1 = documentContext.read("[0].name");
        assertThat(name1).isEqualTo(john.name());

        String name2 = documentContext.read("[1].name");
        assertThat(name2).isEqualTo(jean.name());

        //Or ObjectMapper
        String expected = objectMapper.writeValueAsString(List.of(john, jean));
        JSONAssert.assertEquals(expected, response.getBody(), true);
    }

    @Test
    void shouldReturnAnEmptyUserList() {
        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //Can use DocumentContext
        DocumentContext documentContext = JsonPath.parse(response.getBody());

        int userCount = documentContext.read("$.length()");
        assertThat(userCount).isEqualTo(0);

        //Or
        assertThat(response.getBody()).isEqualTo("[]");
    }

    @Test
    @DirtiesContext
    void shouldReturnTheUser() throws Exception {
        User john = new User(1, "John Doe", "johnD", "john.doe@mail.com");
        userRepository.save(john);

        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL + "/" + john.id(), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //Can use DocumentContext
        DocumentContext documentContext = JsonPath.parse(response.getBody());

        int id = documentContext.read("$.id");
        assertThat(id).isEqualTo(john.id());

        String name = documentContext.read("$.name");
        assertThat(name).isEqualTo(john.name());

        //Or ObjectMapper
        String expected = objectMapper.writeValueAsString(john);
        JSONAssert.assertEquals(expected, response.getBody(), true);
    }

    @Test
    @DirtiesContext
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        int userId = 99;
        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL + "/" + userId, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        //Can use DocumentContext
        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String message = documentContext.read("$.detail");
        assertThat(message).isEqualTo(Constants.USER_NOT_FOUND_EXCEPTION_MESSAGE + userId);

        //Or ObjectMapper
        String expected = "{\"type\":\"about:blank\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"User not found with the id: 99\",\"instance\":\"/api/v1/users/99\"}";
        JSONAssert.assertEquals(expected, response.getBody(), true);
    }

    @Test
    void shouldCreateUser() {
        User john = new User(null, "John Doe", "johnD", "john.doe@mail.com");

        ResponseEntity<Void> response = restTemplate.postForEntity(BASE_URL, john, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();

        ResponseEntity<String> responseUser = restTemplate.getForEntity(response.getHeaders().getLocation(), String.class);

        DocumentContext documentContext = JsonPath.parse(responseUser.getBody());

        Integer id = documentContext.read("$.id");
        assertThat(id).isNotNull();

        String name = documentContext.read("$.name");
        assertThat(name).isEqualTo(john.name());

        String username = documentContext.read("$.username");
        assertThat(username).isEqualTo(john.username());

        String email = documentContext.read("$.email");
        assertThat(email).isEqualTo(john.email());
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyIsInvalid() {
        User john = new User(null, null, null, null);

        ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL, john, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String errorMessage = documentContext.read("$.detail");
        assertThat(errorMessage).contains(NAME_CANNOT_BE_BLANK);
        assertThat(errorMessage).contains(USERNAME_CANNOT_BE_BLANK);
        assertThat(errorMessage).contains(EMAIL_CANNOT_BE_BLANK);
    }

    @Test
    @DirtiesContext
    void shouldReturnConflictWhenUserWithSameUserNameAlreadyExist() {
        User john = new User(null, "John Doe", "johnD", "john.doe@mail.com");
        userRepository.save(john);

        User anotherJohn = new User(null, "John Dean", "johnD", "john.dean@mail.com");

        ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL, anotherJohn, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String errorMessage = documentContext.read("$.detail");
        assertThat(errorMessage).isEqualTo(USER_ALREADY_EXISTS_EXCEPTION_MESSAGE + anotherJohn.username());
    }

    @Test
    @DirtiesContext
    void shouldUpdateTheUser() {
        User user = userRepository.save(new User(null, "John", "johnD", "john.doe@mail.com"));
        User userToUpdate = new User(user.id(), "John Doe", "johnD", "new.john.doe@mail.com");

		ResponseEntity<String> response = restTemplate
                .exchange(BASE_URL + "/" + userToUpdate.id(),
                        HttpMethod.PUT,
                        new HttpEntity<>(userToUpdate),
                        String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		Optional<User> updatedUser = userRepository.findById(userToUpdate.id());
		assertThat(updatedUser).isPresent();
		assertThat(updatedUser.get().name()).isEqualTo(userToUpdate.name());
		assertThat(updatedUser.get().username()).isEqualTo(userToUpdate.username());
		assertThat(updatedUser.get().email()).isEqualTo(userToUpdate.email());
	}
}
