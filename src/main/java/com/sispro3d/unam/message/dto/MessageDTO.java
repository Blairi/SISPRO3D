package com.sispro3d.unam.message.dto;

import com.sispro3d.unam.core.dto.AccountRef;
import com.sispro3d.unam.core.dto.ThreadRef;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private int id;
    private ThreadRef thread;
    private AccountRef account;
    private String content;
    private LocalDateTime timeStamp;

    public MessageDTO(int id) {
        this.id = id;
    }
}
