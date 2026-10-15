package com.sispro3d.unam.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Cuerpo de creación de una orden. El cliente es la cuenta del token de
 * acceso, por eso no se envía en la petición.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrderCreateDTO {

    @NotNull(message = "quoteId is required")
    private Long quoteId;
}
