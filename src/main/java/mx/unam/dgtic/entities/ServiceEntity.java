package mx.unam.dgtic.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "service")
public class ServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "title")
    private String title;

    @NotBlank
    @Size(max = 1000)
    @Column(name = "description")
    private String description;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "base_price")
    private BigDecimal basePrice;

    @OneToOne
    @JoinColumn(name = "id_admin")
    private AdminEntity admin;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_expert")
    private ExpertEntity expert;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Min(1)
    @Column(name = "delivery_time_days")
    private Integer deliveryTimeDays;
}