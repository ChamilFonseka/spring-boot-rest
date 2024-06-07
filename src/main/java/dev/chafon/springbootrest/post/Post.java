package dev.chafon.springbootrest.post;

import jakarta.validation.constraints.NotBlank;

import static dev.chafon.springbootrest.Constants.*;

public record Post(
        Integer id,
        @NotBlank(message = POST_USER_CANNOT_BE_BLANK)
        Integer userId,
        @NotBlank(message = POST_TITLE_CANNOT_BE_BLANK)
        String title,
        @NotBlank(message = POST_BODY_CANNOT_BE_BLANK)
        String body) {
}
