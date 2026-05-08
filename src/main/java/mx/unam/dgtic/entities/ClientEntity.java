package mx.unam.dgtic.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "client")
public class ClientEntity {
    @Id
    @Column(name = "id_user")
    private Integer idUser;

    @OneToOne
    @NotNull
    @MapsId  // PK de AccountEntity
    @JoinColumn(name = "id_user")
    private AccountEntity account;
}