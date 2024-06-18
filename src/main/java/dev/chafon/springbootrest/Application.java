package dev.chafon.springbootrest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.chafon.springbootrest.post.Post;
import dev.chafon.springbootrest.post.PostService;
import dev.chafon.springbootrest.user.User;
import dev.chafon.springbootrest.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.io.InputStream;
import java.util.List;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	@Profile("dev")
	CommandLineRunner commandLineRunner(UserService userService,
										PostService postService,
										ObjectMapper objectMapper) {
		return args -> {
			String usersJson = "/data/users.json";
            try (InputStream inputStream = TypeReference.class.getResourceAsStream(usersJson)) {
				List<User> users = objectMapper.readValue(inputStream, new TypeReference<>() {});
				users.forEach(user -> userService.createUser(
						new User(null, user.name(), user.username(), user.email())
				));
			}

			String postsJson = "/data/posts.json";
			try (InputStream inputStream = TypeReference.class.getResourceAsStream(postsJson)) {
				List<Post> posts = objectMapper.readValue(inputStream, new TypeReference<>() {});
				posts.forEach(post -> postService.createPost(
						new Post(null, post.userId(), post.title(), post.body())
				));
			}
		};
	}

}
