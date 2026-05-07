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

    public ClientDTO(int idUser) {
        this.account = new AccountDTO();
        this.account.setIdUser(idUser);
    }
}
