package com.sispro3d.unam.message.domain;

import com.sispro3d.unam.thread.domain.Thread;
import com.sispro3d.unam.user.domain.Account;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "message")
@Getter
@Setter
@ToString(exclude = {"thread", "account"})
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_thread", nullable = false)
    private Thread thread;

    /**
     * The participant (client or expert) who sent this message.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Account account;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "time_stamp", updatable = false)
    private LocalDateTime timestamp;
}
