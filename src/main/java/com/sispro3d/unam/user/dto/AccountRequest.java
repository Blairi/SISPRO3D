package com.sispro3d.unam.user.dto;

import com.sispro3d.unam.user.domain.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRequest {
    private String name;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private Role role;
    private String specialty;
    private String portfolioUrl;
    private String bio;
    private Integer yearsExperience;
}
