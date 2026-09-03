package com.sispro3d.unam.api.dto;

import com.sispro3d.unam.user.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRequestDTO {

    @NotBlank(message = "name is required")
    @Size(max = 50, message = "name must be at most 50 characters")
    private String name;

    @NotBlank(message = "lastName is required")
    @Size(max = 50, message = "lastName must be at most 50 characters")
    private String lastName;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    @Size(max = 50, message = "email must be at most 50 characters")
    private String email;

    @Size(max = 20, message = "phone must be at most 20 characters")
    private String phone;

    @NotBlank(message = "password is required")
    @Size(min = 4, message = "password must be at least 4 characters")
    private String password;

    @jakarta.validation.constraints.NotNull(message = "role is required")
    private Role role;

    @Size(max = 100, message = "specialty must be at most 100 characters")
    private String specialty;

    private String portfolioUrl;
    private String bio;

    @jakarta.validation.constraints.Min(value = 0, message = "yearsExperience must be non-negative")
    private Integer yearsExperience;
}
