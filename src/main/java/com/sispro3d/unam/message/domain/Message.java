package com.sispro3d.unam.message.domain;

import com.sispro3d.unam.thread.domain.Thread;
import com.sispro3d.unam.user.domain.Account;
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