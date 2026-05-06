package mx.unam.dgtic.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "deliverable")
public class DeliverableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "url_file")
    private String urlFile;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "file_type")
    private String fileType;

    @ManyToOne
    @JoinColumn(name = "id_order")
    private WorkOrderEntity workOrder;
}