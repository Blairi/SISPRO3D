package mx.unam.dgtic.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "review")
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(name = "rating")
    private int rating;

    @Size(max = 1000)
    @Column(name = "comment")
    private String comment;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_client")
    private ClientEntity client;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_service")
    private ServiceEntity service;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}