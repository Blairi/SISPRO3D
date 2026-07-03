package com.sispro3d.unam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreadDTO {
    private int id;
    private WorkOrderDTO workOrder;

    public ThreadDTO(int id) {
        this.id = id;
    }
}
