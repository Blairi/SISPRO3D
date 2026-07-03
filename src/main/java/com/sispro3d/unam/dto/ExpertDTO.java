package com.sispro3d.unam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpertDTO {
    private AccountDTO account;
    private String specialty;
    private String portfolioUrl;
    private String bio;
    private int yearsExperience;

    public ExpertDTO(int idUser) {
        this.account = new AccountDTO();
        this.account.setIdUser(idUser);
    }
}
