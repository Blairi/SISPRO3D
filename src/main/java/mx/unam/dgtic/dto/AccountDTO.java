package mx.unam.dgtic.dto;

import lombok.*;
import mx.unam.dgtic.domain.UserType;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
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