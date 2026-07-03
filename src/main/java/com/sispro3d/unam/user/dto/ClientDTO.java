package com.sispro3d.unam.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {
    private AccountDTO account;

    public ClientDTO(int idUser) {
        this.account = new AccountDTO();
        this.account.setIdUser(idUser);
    }
}
