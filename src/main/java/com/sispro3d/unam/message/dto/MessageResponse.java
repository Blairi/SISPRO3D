package com.sispro3d.unam.message.dto;

import com.sispro3d.unam.core.dto.AccountRef;
import com.sispro3d.unam.core.dto.ThreadRef;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
    private int id;
    private ThreadRef thread;
    private AccountRef account;
    private String content;
    private LocalDateTime timeStamp;
}
