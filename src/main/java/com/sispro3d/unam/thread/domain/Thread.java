package com.sispro3d.unam.thread.domain;

import com.sispro3d.unam.workorder.domain.WorkOrder;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Thread {
    private int id;
    private WorkOrder workOrder;
}
