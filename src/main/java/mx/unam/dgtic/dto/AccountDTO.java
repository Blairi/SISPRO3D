package mx.unam.dgtic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.unam.dgtic.domain.UserType;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDTO {
    private int idUser;
    private String name;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private UserType type;
    private LocalDateTime createdAt;
}