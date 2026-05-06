package mx.unam.dgtic.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "message")
public class MessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_thread")
    private ThreadEntity thread;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private AccountEntity account;

    @NotBlank
    @Column(name = "content")
    private String content;

    @Column(name = "time_stamp")
    private LocalDateTime timeStamp;
}