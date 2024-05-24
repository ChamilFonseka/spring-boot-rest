package dev.chafon.springbootrest.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserValidationTest {

    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldPassWhenAllFieldsProvided() {
        User user = new User(1, "John Doe", "johnD", "john.doe@mail.com");
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenAllFieldsNotProvided() {
        User user = new User(1, null, null, null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(3);

        List<String> constraintViolationMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();
        assertThat(constraintViolationMessages).containsExactlyInAnyOrder(
                "Name cannot be blank",
                "Username cannot be blank",
                "Email cannot be blank");
    }

    @Test
    void shouldFailWhenNoNameProvided() {
        User user = new User(1, null, "johnD", "john.doe@mail.com");
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);

        List<String> constraintViolationMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();
        assertThat(constraintViolationMessages).containsExactly("Name cannot be blank");
    }

    @Test
    void shouldFailWhenNoUsernameProvided() {
        User user = new User(1, "John Doe", null, "john.doe@mail.com");
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);

        List<String> constraintViolationMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();
        assertThat(constraintViolationMessages).containsExactly("Username cannot be blank");
    }

    @Test
    void shouldFailWhenNoEmailProvided() {
        User user = new User(1, "John Doe", "johnD", null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);

        List<String> constraintViolationMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();
        assertThat(constraintViolationMessages).containsExactly("Email cannot be blank");
    }

    @Test
    void shouldFailWhenInvalidEmailProvided() {
        User user = new User(1, "John Doe", "johnD", "-");
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);

        List<String> constraintViolationMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();
        assertThat(constraintViolationMessages).containsExactly("Email must be valid");
    }
}
