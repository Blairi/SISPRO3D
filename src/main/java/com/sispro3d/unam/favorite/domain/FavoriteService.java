package com.sispro3d.unam.favorite.domain;

import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.user.domain.Account;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "favorite_service",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_favorite_client_service",
                columnNames = {"id_client", "id_offered_service"}))
@Getter
@Setter
@ToString(exclude = {"client", "service"})
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * The client that bookmarked the offered service.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_client", nullable = false)
    private Account client;

    /**
     * The marked offered service.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_offered_service", nullable = false)
    private OfferedService service;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}