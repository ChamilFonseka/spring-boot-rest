package dev.chafon.springbootrest;

import dev.chafon.springbootrest.post.Post;
import dev.chafon.springbootrest.post.PostService;
import dev.chafon.springbootrest.user.User;
import dev.chafon.springbootrest.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	@Profile("dev")
	CommandLineRunner commandLineRunner(UserService userService, PostService postService) {
		return args -> {
			User john = userService.createUser(new User(null, "John Doe", "johnD", "john.doe@mail.com"));
			User jean = userService.createUser(new User(null, "Jean Gray", "janeG", "jane.gray@mail.com"));
			User bob = userService.createUser(new User(null, "Bob Smith", "bobS", "bob.smith@mail.com"));
			User alice = userService.createUser(new User(null, "Alice Brown", "aliceB", "alice.brown@mail.com"));
			User tom = userService.createUser(new User(null, "Tom Wilson", "tomW", "tom.wilson@mail.com"));

			postService.createPost(new Post(null, john.id(), "Java Multithreading", "Java Multithreading is awesome!"));
			postService.createPost(new Post(null, john.id(), "Java Spring Boot", "Java Spring Boot is great!"));
			postService.createPost(new Post(null, alice.id(), "Intro to LLM", "Welcome to LLM!"));
			postService.createPost(new Post(null, bob.id(), "Data Science", "Data Science is awesome!"));
		};
	}

}
