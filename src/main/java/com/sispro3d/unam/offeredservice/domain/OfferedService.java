package com.sispro3d.unam.offeredservice.domain;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.user.domain.Account;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "offered_service")
@Getter
@Setter
@ToString(exclude = {"admin", "expert", "category"})
@NoArgsConstructor
@AllArgsConstructor
public class OfferedService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_offered_service")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    /**
     * The expert who created and owns this service. Required at creation time.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_expert", nullable = false)
    private Account expert;

    /**
     * The admin who reviewed (approved/rejected) this service.
     * Null while the service is still PENDING.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_admin")
    private Account admin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_category", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceStatus status;

    @Column(name = "delivery_time_days", nullable = false)
    private Integer deliveryTimeDays;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}