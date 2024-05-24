package dev.chafon.springbootrest.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserJsonTest {
    @Autowired
    private JacksonTester<User> json;
    @Autowired
    private JacksonTester<User[]> jsonList;

    private User[] users;

    @BeforeEach
    void setUp() {
        users = new User[] {
                new User(1, "John Doe", "johnD", "john.doe@mail.com"),
                new User(2, "Jane Doe", "janeD", "jane.doe@mail.com")
        };
    }

    @Test
    void testUserSerialization() throws Exception {
        User user = users[0];
        JsonContent<User> jsonContent = json.write(user);

        assertThat(jsonContent).isStrictlyEqualToJson("""
                {
                   "id":1,
                   "name":"John Doe",
                   "username":"johnD",
                   "email":"john.doe@mail.com"
                }
                """);

        assertThat(jsonContent).hasJsonPathNumberValue("$.id");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(user.id());

        assertThat(jsonContent).hasJsonPathStringValue("$.name");
        assertThat(jsonContent).extractingJsonPathStringValue("$.name").isEqualTo(user.name());

        assertThat(jsonContent).hasJsonPathStringValue("$.username");
        assertThat(jsonContent).extractingJsonPathStringValue("$.username").isEqualTo(user.username());

        assertThat(jsonContent).hasJsonPathStringValue("$.email");
        assertThat(jsonContent).extractingJsonPathStringValue("$.email").isEqualTo(user.email());
    }

    @Test
    void testUserDeserialization() throws Exception {
        String jsonString = """
                {
                   "id":1,
                   "name":"John Doe",
                   "username":"johnD",
                   "email":"john.doe@mail.com"
                }
                """;

        assertThat(json.parse(jsonString)).isEqualTo(new User(1, "John Doe", "johnD", "john.doe@mail.com"));

        assertThat(json.parseObject(jsonString).id()).isEqualTo(1);
        assertThat(json.parseObject(jsonString).name()).isEqualTo("John Doe");
        assertThat(json.parseObject(jsonString).username()).isEqualTo("johnD");
        assertThat(json.parseObject(jsonString).email()).isEqualTo("john.doe@mail.com");
    }

    @Test
    void testUserListSerialization() throws Exception {
        JsonContent<User[]> jsonContent = jsonList.write(users);

        assertThat(jsonContent).isStrictlyEqualToJson("""
                [
                   {
                      "id":1,
                      "name":"John Doe",
                      "username":"johnD",
                      "email":"john.doe@mail.com"
                   },
                   {
                      "id":2,
                      "name":"Jane Doe",
                      "username":"janeD",
                      "email":"jane.doe@mail.com"
                   }
                ]
                """);

        assertThat(jsonContent).hasJsonPathNumberValue("[0].id");
        assertThat(jsonContent).extractingJsonPathNumberValue("[0].id").isEqualTo(users[0].id());

        assertThat(jsonContent).hasJsonPathStringValue("[0].name");
        assertThat(jsonContent).extractingJsonPathStringValue("[0].name").isEqualTo(users[0].name());

        assertThat(jsonContent).hasJsonPathStringValue("[0].username");
        assertThat(jsonContent).extractingJsonPathStringValue("[0].username").isEqualTo(users[0].username());

        assertThat(jsonContent).hasJsonPathStringValue("[0].email");
        assertThat(jsonContent).extractingJsonPathStringValue("[0].email").isEqualTo(users[0].email());

        assertThat(jsonContent).hasJsonPathNumberValue("[1].id");
        assertThat(jsonContent).extractingJsonPathNumberValue("[1].id").isEqualTo(users[1].id());

        assertThat(jsonContent).hasJsonPathStringValue("[1].name");
        assertThat(jsonContent).extractingJsonPathStringValue("[1].name").isEqualTo(users[1].name());

        assertThat(jsonContent).hasJsonPathStringValue("[1].username");
        assertThat(jsonContent).extractingJsonPathStringValue("[1].username").isEqualTo(users[1].username());

        assertThat(jsonContent).hasJsonPathStringValue("[1].email");
        assertThat(jsonContent).extractingJsonPathStringValue("[1].email").isEqualTo(users[1].email());
    }

    @Test
    void testUserListDeserialization() throws Exception {
        String jsonString = """
                [
                   {
                      "id":1,
                      "name":"John Doe",
                      "username":"johnD",
                      "email":"john.doe@mail.com"
                   },
                   {
                      "id":2,
                      "name":"Jane Doe",
                      "username":"janeD",
                      "email":"jane.doe@mail.com"
                   }
                ]
                """;

        assertThat(jsonList.parse(jsonString)).isEqualTo(users);

        assertThat(jsonList.parseObject(jsonString)[0].id()).isEqualTo(1);
        assertThat(jsonList.parseObject(jsonString)[0].name()).isEqualTo("John Doe");
        assertThat(jsonList.parseObject(jsonString)[0].username()).isEqualTo("johnD");
        assertThat(jsonList.parseObject(jsonString)[0].email()).isEqualTo("john.doe@mail.com");

        assertThat(jsonList.parseObject(jsonString)[1].id()).isEqualTo(2);
        assertThat(jsonList.parseObject(jsonString)[1].name()).isEqualTo("Jane Doe");
        assertThat(jsonList.parseObject(jsonString)[1].username()).isEqualTo("janeD");
        assertThat(jsonList.parseObject(jsonString)[1].email()).isEqualTo("jane.doe@mail.com");
    }
}
