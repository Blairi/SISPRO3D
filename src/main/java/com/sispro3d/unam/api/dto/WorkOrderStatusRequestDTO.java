package com.sispro3d.unam.api.dto;

import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrderStatusRequestDTO {

    @NotNull(message = "status is required")
    private WorkOrderStatus status;
}
