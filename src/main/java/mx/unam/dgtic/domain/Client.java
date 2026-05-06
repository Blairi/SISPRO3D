package mx.unam.dgtic.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    private Account account;

    public Client(int idUser) {
        this.account = new Account(idUser);
    }
}