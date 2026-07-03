package com.sispro3d.unam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
