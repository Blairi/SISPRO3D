package com.sispro3d.unam.thread.domain;

import com.sispro3d.unam.workorder.domain.WorkOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Thread {
    private int id;
    private WorkOrder workOrder;

    public Thread(int id) {
        this.id = id;
    }
}
