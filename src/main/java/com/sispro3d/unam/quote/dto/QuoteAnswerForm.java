package com.sispro3d.unam.quote.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Formulario con el que el experto responde una cotización: establece el monto
 * total y la fecha hasta la que esa propuesta sigue siendo válida.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteAnswerForm {

    @NotNull(message = "{ans.NotNull.totalAmount}")
    @DecimalMin(value = "0.01", message = "{ans.DecimalMin.totalAmount}")
    private BigDecimal totalAmount;

    @NotNull(message = "{ans.NotNull.validUntil}")
    private LocalDate validUntil;
}