package mx.unam.dgtic.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "admin")
public class AdminEntity {
    @Id
    @Column(name = "id_user")
    private int idUser;

    @OneToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private AccountEntity account;
}