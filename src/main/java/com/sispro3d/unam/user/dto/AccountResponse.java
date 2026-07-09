package com.sispro3d.unam.user.dto;

import com.sispro3d.unam.user.domain.UserType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {
    private int idUser;
    private String name;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private UserType type;
    private LocalDateTime createdAt;
}
