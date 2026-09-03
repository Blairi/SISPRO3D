package com.sispro3d.unam.api.dto;

import com.sispro3d.unam.user.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponseDTO {

    private Long idUser;
    private String name;
    private String lastName;
    private String email;
    private String phone;
    private Role role;
    private String specialty;
    private String portfolioUrl;
    private String bio;
    private Integer yearsExperience;
    private LocalDateTime createdAt;
}
