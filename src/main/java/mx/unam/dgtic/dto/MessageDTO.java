package mx.unam.dgtic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {
    private int id;
    private ThreadDTO thread;
    private AccountDTO account;
    private String content;
    private LocalDateTime timeStamp;

    public MessageDTO(int id) {
        this.id = id;
    }
}
