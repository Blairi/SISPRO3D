package com.sispro3d.unam.preview.domain;

import com.sispro3d.unam.deliverable.domain.Deliverable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "preview")
@Getter
@Setter
@ToString(exclude = "deliverable")
@NoArgsConstructor
@AllArgsConstructor
public class Preview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String caption;

    @Column(name = "url_file", nullable = false)
    private String urlFile;

    /**
     * The render/asset this preview illustrates.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deliverable_id", nullable = false)
    private Deliverable deliverable;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
