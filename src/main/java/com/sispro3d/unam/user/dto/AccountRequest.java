package com.sispro3d.unam.user.dto;

import com.sispro3d.unam.user.domain.Role;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRequest {

    @NotBlank(message = "{acc.NotBlank.name}")
    @Size(max = 50, message = "{acc.Size.name}")
    private String name;

    @NotBlank(message = "{acc.NotBlank.lastName}")
    @Size(max = 50, message = "{acc.Size.lastName}")
    private String lastName;

    @NotBlank(message = "{acc.NotBlank.email}")
    @Email(message = "{acc.Email.email}")
    @Size(max = 50, message = "{acc.Size.email}")
    private String email;

    @Size(max = 20, message = "{acc.Size.phone}")
    private String phone;

    private String password;

    @NotNull(message = "{acc.NotNull.role}")
    private Role role;

    @Size(max = 100, message = "{acc.Size.specialty}")
    private String specialty;

    @Size(max = 255, message = "{acc.Size.portfolioUrl}")
    private String portfolioUrl;

    @Size(max = 500, message = "{acc.Size.bio}")
    private String bio;

    @Min(value = 0, message = "{acc.Min.yearsExperience}")
    @Max(value = 100, message = "{acc.Max.yearsExperience}")
    private Integer yearsExperience;
}
