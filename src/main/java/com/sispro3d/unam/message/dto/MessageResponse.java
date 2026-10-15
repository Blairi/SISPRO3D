package com.sispro3d.unam.message.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
    private Long id;
    private Long threadId;
    private Long userId;
    private String content;
    private LocalDateTime timestamp;
}
