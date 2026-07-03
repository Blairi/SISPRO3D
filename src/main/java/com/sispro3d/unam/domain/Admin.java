package com.sispro3d.unam.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin {
    private Account account;

    public Admin(int idUser) {
        this.account = new Account(idUser);
    }
}