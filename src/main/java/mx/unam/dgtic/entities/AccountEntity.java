package mx.unam.dgtic.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.unam.dgtic.domain.UserType;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "account")
public class AccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Integer idUser;

    @NotBlank
    @Size(max = 50)
    @Column(name = "name")
    private String name;

    @NotBlank
    @Size(max = 50)
    @Column(name = "lastName")
    private String lastName;

    @NotBlank
    @Email
    @Column(name = "email")
    private String email;

    @NotBlank
    @Size(max = 15)
    @Column(name = "phone")
    private String phone;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password")
    private String password;

    @NotNull
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private UserType type; // ADMIN, CLIENT, EXPERT

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
