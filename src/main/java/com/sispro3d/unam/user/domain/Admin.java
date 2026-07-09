package com.sispro3d.unam.user.domain;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Admin {
    private Account account;

    public Admin(int idUser) {
        this.account = new Account(idUser);
    }
}