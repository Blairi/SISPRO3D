package com.sispro3d.unam.user.dto;

import com.sispro3d.unam.user.domain.UserType;
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
    private UserType type;
}
