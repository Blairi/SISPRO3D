package com.sispro3d.unam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDTO {
    private AccountDTO account;

    public AdminDTO(int idUser) {
        this.account = new AccountDTO();
        this.account.setIdUser(idUser);
    }
}
