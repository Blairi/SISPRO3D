package com.sispro3d.unam.user.domain;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    private Account account;

    public Client(int idUser) {
        this.account = new Account(idUser);
    }
}