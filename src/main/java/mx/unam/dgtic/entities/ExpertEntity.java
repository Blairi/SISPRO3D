package mx.unam.dgtic.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "expert")
public class ExpertEntity {
    @Id
    @Column(name = "id_user")
    private int idUser;

    @OneToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private AccountEntity account;

    @Column(name = "specialty")
    private String specialty;

    @Column(name = "portfolio_url")
    private String portfolioUrl;

    @Column(name = "bio")
    private String bio;

    @Column(name = "years_experience")
    private Integer yearsExperience;
}