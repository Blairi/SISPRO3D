package com.sispro3d.unam.deliverable.domain;

import com.sispro3d.unam.workorder.domain.WorkOrder;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliverable")
@Getter
@Setter
@ToString(exclude = "workOrder")
@NoArgsConstructor
@AllArgsConstructor
public class Deliverable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(name = "url_file", nullable = false)
    private String urlFile;

    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * The work order this file belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_order", nullable = false)
    private WorkOrder workOrder;
}
