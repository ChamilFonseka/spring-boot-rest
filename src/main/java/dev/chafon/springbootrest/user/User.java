package dev.chafon.springbootrest.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import static dev.chafon.springbootrest.Constants.*;

public record User(
        Integer id,
        @NotBlank(message = NAME_CANNOT_BE_BLANK)
        String name,
        @NotBlank(message = USERNAME_CANNOT_BE_BLANK)
        String username,
        @Email(message = EMAIL_MUST_BE_VALID)
        @NotBlank(message = EMAIL_CANNOT_BE_BLANK)
        String email
) {
}
