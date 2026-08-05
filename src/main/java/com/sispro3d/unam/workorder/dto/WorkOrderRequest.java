package com.sispro3d.unam.workorder.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrderRequest {
    private Long clientId;
    private Long quoteId;
}
