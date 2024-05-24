package dev.chafon.springbootrest.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record User(
        Integer id,
        @NotBlank(message = "Name cannot be blank")
        String name,
        @NotBlank(message = "Username cannot be blank")
        String username,
        @Email(message = "Email must be valid")
        @NotBlank(message = "Email cannot be blank")
        String email
) {
}
