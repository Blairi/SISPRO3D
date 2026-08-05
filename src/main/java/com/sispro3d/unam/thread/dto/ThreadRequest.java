package com.sispro3d.unam.thread.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreadRequest {
    private Long actorId;
    private Long workOrderId;
}
