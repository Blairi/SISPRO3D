package com.sispro3d.unam.review.domain;

import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.user.domain.Account;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "review") // TODO: Agregar constraint de id_client y id_service
@Getter
@Setter
@ToString(exclude = {"client", "offeredService"})
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_review")
    private Long id;

    // TODO: validar que la reseña este entre 1 y 5
    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_client", nullable = false)
    private Account client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_offered_service", nullable = false)
    private OfferedService offeredService;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}