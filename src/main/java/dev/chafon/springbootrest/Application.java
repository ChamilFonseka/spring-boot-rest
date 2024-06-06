package dev.chafon.springbootrest;

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
	CommandLineRunner commandLineRunner(UserService userService) {
		return args -> {
			userService.createUser(new User(null, "John Doe", "johnD", "john.doe@mail.com"));
			userService.createUser(new User(null, "Jean Gray", "janeG", "jane.gray@mail.com"));
			userService.createUser(new User(null, "Bob Smith", "bobS", "bob.smith@mail.com"));
			userService.createUser(new User(null, "Alice Brown", "aliceB", "alice.brown@mail.com"));
			userService.createUser(new User(null, "Tom Wilson", "tomW", "tom.wilson@mail.com"));
		};
	}

}
