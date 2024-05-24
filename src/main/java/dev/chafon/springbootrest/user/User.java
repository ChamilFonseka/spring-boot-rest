package dev.chafon.springbootrest.user;

public record User(
        Integer id,
        String name,
        String username,
        String email
) {
}
