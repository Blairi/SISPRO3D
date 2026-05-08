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
public class AdminDTO {
    private AccountDTO account;

    public static AdminDTO ofId(int idAdmin) {
        AccountDTO acc = new AccountDTO();
        acc.setIdUser(idAdmin);
        return AdminDTO.builder().account(acc).build();
    }
}