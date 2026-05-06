package mx.unam.dgtic.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull
    @OneToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user")
    private AccountEntity account;

    @NotBlank
    @Size(max = 100)
    @Column(name = "specialty")
    private String specialty;

    @Size(max = 255)
    @Column(name = "portfolio_url")
    private String portfolioUrl;

    @Column(name = "bio")
    @Size(max = 1000)
    private String bio;

    @Min(0)
    @Column(name = "years_experience")
    private Integer yearsExperience;
}