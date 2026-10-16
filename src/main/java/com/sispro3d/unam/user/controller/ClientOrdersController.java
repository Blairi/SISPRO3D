package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.security.CurrentUser;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import com.sispro3d.unam.workorder.dto.WorkOrderResponse;
import com.sispro3d.unam.workorder.service.WorkOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vista de órdenes del cliente. Solo se listan las suyas y las acciones que
 * corresponden a cada estado; el dominio rechaza cualquier acción de otra
 * parte y el rechazo se muestra como mensaje.
 */
@Controller
@RequestMapping("/client/orders")
@Slf4j
public class ClientOrdersController {

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private OfferedServiceService offeredServiceService;

    @Autowired
    private CurrentUser currentUser;

    @GetMapping
    public String list(Model model) {
        Long userId = currentUser.id();
        log.debug("GET /client/orders - usuario={}", userId);

        List<WorkOrderResponse> orders = workOrderService.findByClientId(userId);

        Map<Long, String> titles = new HashMap<>();
        for (WorkOrderResponse order : orders) {
            titles.computeIfAbsent(order.getOfferedServiceId(), id ->
                    offeredServiceService.findById(id)
                            .map(OfferedServiceResponse::getTitle)
                            .orElse("Servicio"));
        }

        model.addAttribute("activeOrders", orders.stream()
                .filter(o -> o.getStatus() == WorkOrderStatus.PENDING
                        || o.getStatus() == WorkOrderStatus.IN_PROGRESS
                        || o.getStatus() == WorkOrderStatus.IN_REVIEW).toList());
        model.addAttribute("finishedOrders", orders.stream()
                .filter(o -> o.getStatus() == WorkOrderStatus.COMPLETED
                        || o.getStatus() == WorkOrderStatus.CANCELED).toList());
        model.addAttribute("serviceTitles", titles);
        return "client/orders";
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, RedirectAttributes attributes) {
        Long userId = currentUser.id();
        log.debug("POST /client/orders/{}/complete - usuario={}", id, userId);
        return run(attributes, () -> workOrderService.complete(id, userId), "Orden completada.");
    }

    @PostMapping("/{id}/request-changes")
    public String requestChanges(@PathVariable Long id, RedirectAttributes attributes) {
        Long userId = currentUser.id();
        log.debug("POST /client/orders/{}/request-changes - usuario={}", id, userId);
        return run(attributes, () -> workOrderService.requestChanges(id, userId),
                "Solicitaste cambios en el trabajo.");
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes attributes) {
        Long userId = currentUser.id();
        log.debug("POST /client/orders/{}/cancel - usuario={}", id, userId);
        return run(attributes, () -> workOrderService.cancel(id, userId), "Orden cancelada.");
    }

    /**
     * Ejecuta la transición y convierte el rechazo del dominio en un mensaje,
     * de modo que un cliente no pierde el contexto al ser devuelto a la lista.
     */
    private String run(RedirectAttributes attributes, Runnable transition, String successMessage) {
        try {
            transition.run();
            attributes.addFlashAttribute("success", successMessage);
        } catch (RuntimeException ex) {
            attributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/client/orders";
    }
}