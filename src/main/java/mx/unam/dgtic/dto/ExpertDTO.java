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

    public static ExpertDTO ofId(int idExpert) {
        AccountDTO acc = new AccountDTO();
        acc.setIdUser(idExpert);
        return ExpertDTO.builder().account(acc).build();
    }

    @Override
    public String toString() {
        return "ExpertDTO{" +
                "id=" + account.getIdUser() +
                ", specialty='" + specialty + '\'' +
                ", portfolioUrl='" + portfolioUrl + '\'' +
                ", bio='" + bio + '\'' +
                ", yearsExperience=" + yearsExperience +
                '}';
    }
}
