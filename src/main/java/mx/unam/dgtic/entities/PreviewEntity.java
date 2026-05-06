package mx.unam.dgtic.entities;

import jakarta.persistence.*;
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

    @Column(name = "caption")
    private String caption;

    @Column(name = "url_file")
    private String urlFile;

    @ManyToOne
    @JoinColumn(name = "deliverable_id")
    private DeliverableEntity deliverable;
}