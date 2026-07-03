package com.sispro3d.unam.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private int idUser;
    private String name;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private UserType type; // ADMIN, CLIENT, EXPERT
    private LocalDateTime createdAt;

    public Account(int idUser) {
        this.idUser = idUser;
    }
}
