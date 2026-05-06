package mx.unam.dgtic.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "deliverable")
public class DeliverableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @NotBlank
    @Size(max = 60)
    @Column(name = "name")
    private String name;

    @NotBlank
    @Size(max = 255)
    @Column(name = "url_file")
    private String urlFile;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @NotBlank
    @Size(max = 50)
    @Column(name = "file_type")
    private String fileType;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_order")
    private WorkOrderEntity workOrder;
}