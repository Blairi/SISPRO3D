package com.sispro3d.unam.core.dto;

import com.sispro3d.unam.user.domain.Role;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRef {
    private int idUser;
    private String name;
    private String lastName;
    private String email;
    private String phone;
    private Role role;
    private String specialty;
    private String portfolioUrl;
    private String bio;
    private Integer yearsExperience;
}
