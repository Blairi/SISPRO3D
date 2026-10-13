package com.sispro3d.unam.message.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequest {
    private Long userId;

    @NotBlank(message = "{msg.NotBlank.content}")
    private String content;
    private Long threadId;
}
