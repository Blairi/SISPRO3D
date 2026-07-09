package com.sispro3d.unam.core.dto;

import com.sispro3d.unam.user.domain.UserType;
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
    private UserType type;
}
