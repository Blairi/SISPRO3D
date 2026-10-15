package com.sispro3d.unam.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RegisterRequestValidationTest {

    @Autowired
    private Validator validator;

    private RegisterRequest validClientRequest() {
        return RegisterRequest.builder()
                .name("Juan")
                .lastName("Pérez")
                .email("juan@test.com")
                .password("secret123")
                .role("CLIENT")
                .build();
    }

    private RegisterRequest validExpertRequest() {
        return RegisterRequest.builder()
                .name("Ana")
                .lastName("García")
                .email("ana@test.com")
                .password("secret123")
                .role("EXPERT")
                .specialty("Modelado 3D")
                .yearsExperience(5)
                .build();
    }

    @Test
    void validClientRequest_noErrors() {
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validClientRequest());
        assertThat(violations).isEmpty();
    }

    @Test
    void validExpertRequest_noErrors() {
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validExpertRequest());
        assertThat(violations).isEmpty();
    }

    @Test
    void blankName_returnsError() {
        var request = validClientRequest();
        request.setName("");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    void blankLastName_returnsError() {
        var request = validClientRequest();
        request.setLastName("");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("lastName"));
    }

    @Test
    void blankEmail_returnsError() {
        var request = validClientRequest();
        request.setEmail("");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void invalidEmail_returnsError() {
        var request = validClientRequest();
        request.setEmail("no-es-email");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void blankPassword_returnsError() {
        var request = validClientRequest();
        request.setPassword("");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void shortPassword_returnsError() {
        var request = validClientRequest();
        request.setPassword("ab");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void blankRole_returnsError() {
        var request = validClientRequest();
        request.setRole("");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("role"));
    }

    @Test
    void invalidRole_returnsError() {
        var request = validClientRequest();
        request.setRole("ADMIN");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("role"));
    }

    @Test
    void bioTooLong_returnsError() {
        var request = validExpertRequest();
        request.setBio("a".repeat(501));

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("bio"));
    }

    @Test
    void negativeYearsExperience_returnsError() {
        var request = validExpertRequest();
        request.setYearsExperience(-1);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("yearsExperience"));
    }

    @Test
    void yearsExperienceOver100_returnsError() {
        var request = validExpertRequest();
        request.setYearsExperience(101);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("yearsExperience"));
    }
}
