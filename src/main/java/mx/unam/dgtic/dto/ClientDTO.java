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
public class ClientDTO {
    private AccountDTO account;

    public static ClientDTO ofId(int idUser) {
        AccountDTO acc = new AccountDTO();
        acc.setIdUser(idUser);
        return ClientDTO.builder().account(acc).build();
    }
}
