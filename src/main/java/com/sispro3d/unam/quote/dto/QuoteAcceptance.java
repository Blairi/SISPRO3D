package com.sispro3d.unam.quote.dto;

import com.sispro3d.unam.workorder.dto.WorkOrderResponse;

/**
 * Resultado de aceptar una cotización: la cotización resuelta junto con la
 * orden de trabajo que esa aceptación crea.
 */
public record QuoteAcceptance(QuoteResponse quote, WorkOrderResponse workOrder) {
}