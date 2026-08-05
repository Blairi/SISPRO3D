package com.sispro3d.unam.message.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequest {
    private Long userId;
    private Long threadId;
    private String content;
}
