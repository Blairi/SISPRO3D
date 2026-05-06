package mx.unam.dgtic.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expert {
    private Account account;
    private String specialty;
    private String portfolioUrl;
    private String bio;
    private int yearsExperience;

    public Expert(int idUser) {
        this.account = new Account(idUser);
    }
}