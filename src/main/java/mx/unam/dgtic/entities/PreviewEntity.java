package mx.unam.dgtic.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "preview")
public class PreviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "caption")
    private String caption;

    @NotBlank
    @Size(max = 255)
    @Column(name = "url_file")
    private String urlFile;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "deliverable_id")
    private DeliverableEntity deliverable;
}