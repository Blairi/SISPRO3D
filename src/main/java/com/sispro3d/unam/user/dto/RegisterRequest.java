package com.sispro3d.unam.user.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "{NotBlank.name}")
    @Size(max = 50, message = "{Size.name}")
    private String name;

    @NotBlank(message = "{NotBlank.lastName}")
    @Size(max = 50, message = "{Size.lastName}")
    private String lastName;

    @NotBlank(message = "{NotBlank.email}")
    @Email(message = "{Email.email}")
    @Size(max = 50, message = "{Size.email}")
    @UniqueEmail(message = "{UniqueEmail.email}")
    private String email;

    private String phone;

    @NotBlank(message = "{NotBlank.password}")
    @Size(min = 6, message = "{Size.password}")
    private String password;

    @NotBlank(message = "{NotBlank.role}")
    @Pattern(regexp = "^(CLIENT|EXPERT)$", message = "{Pattern.role}")
    private String role;

    private String specialty;

    private String portfolioUrl;

    @Size(max = 500, message = "{Size.bio}")
    private String bio;

    @Min(value = 0, message = "{Min.yearsExperience}")
    @Max(value = 100, message = "{Max.yearsExperience}")
    private Integer yearsExperience;
}
