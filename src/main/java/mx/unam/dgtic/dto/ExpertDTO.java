package mx.unam.dgtic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
