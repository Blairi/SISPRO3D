package com.sispro3d.unam.thread.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreadResponse {
    private Long id;
    private Long workOrderId;
}
