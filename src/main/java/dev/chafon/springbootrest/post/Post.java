package dev.chafon.springbootrest.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import static dev.chafon.springbootrest.Constants.*;

public record Post(
        Integer id,
        @NotNull(message = POST_USER_CANNOT_BE_NULL)
        Integer userId,
        @NotBlank(message = POST_TITLE_CANNOT_BE_BLANK)
        String title,
        @NotBlank(message = POST_BODY_CANNOT_BE_BLANK)
        String body) {
}
