package mx.unam.dgtic.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.unam.dgtic.domain.QuoteStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "quote")
public class QuoteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private QuoteStatus status;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @FutureOrPresent
    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Size(max = 1000)
    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_client")
    private ClientEntity client;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_service")
    private ServiceEntity service;
}