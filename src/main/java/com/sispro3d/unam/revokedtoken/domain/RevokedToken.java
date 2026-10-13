package com.sispro3d.unam.revokedtoken.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Identificador de un token de refresco que ya no puede canjearse, para que un
 * logout invalide el token del cliente aunque su vigencia aún no haya vencido.
 */
@Entity
@Table(name = "revoked_token")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RevokedToken {

    @Id
    @Column(name = "id_token", length = 64)
    private String id;

    @Column(name = "revoked_at", nullable = false)
    private LocalDateTime revokedAt;

    public RevokedToken(String id) {
        this.id = id;
        this.revokedAt = LocalDateTime.now();
    }
}
