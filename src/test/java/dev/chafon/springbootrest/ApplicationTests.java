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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

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
	void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception{
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
}
