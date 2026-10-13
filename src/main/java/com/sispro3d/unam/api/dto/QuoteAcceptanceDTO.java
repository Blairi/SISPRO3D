package com.sispro3d.unam.api.dto;

import lombok.*;

/**
 * Respuesta de aceptar una cotización: la cotización resuelta junto con la
 * orden de trabajo que crea esa aceptación.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteAcceptanceDTO {
    private QuoteResponseDTO quote;
    private WorkOrderResponseDTO workOrder;
}
