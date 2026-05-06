package mx.unam.dgtic.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private int id;
    private Thread thread;
    private Account account;
    private String content;
    private LocalDateTime timeStamp;

    public Message(int id) {
        this.id = id;
    }
}