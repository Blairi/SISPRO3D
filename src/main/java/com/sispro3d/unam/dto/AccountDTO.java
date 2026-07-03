package com.sispro3d.unam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.sispro3d.unam.domain.UserType;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private int idUser;
    private String name;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private UserType type;
    private LocalDateTime createdAt;
}