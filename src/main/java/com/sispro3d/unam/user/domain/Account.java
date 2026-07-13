package com.sispro3d.unam.user.domain;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private int idUser;
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
    private LocalDateTime createdAt;

    public Account(int idUser) {
        this.idUser = idUser;
    }
}
