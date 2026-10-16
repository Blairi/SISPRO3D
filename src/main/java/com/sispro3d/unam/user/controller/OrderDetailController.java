package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.deliverable.dto.DeliverableResponse;
import com.sispro3d.unam.deliverable.service.DeliverableService;
import com.sispro3d.unam.message.dto.MessageRequest;
import com.sispro3d.unam.message.dto.MessageResponse;
import com.sispro3d.unam.message.service.MessageService;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.preview.dto.PreviewResponse;
import com.sispro3d.unam.preview.service.PreviewService;
import com.sispro3d.unam.quote.dto.QuoteResponse;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.security.CurrentUser;
import com.sispro3d.unam.thread.dto.ThreadRequest;
import com.sispro3d.unam.thread.dto.ThreadResponse;
import com.sispro3d.unam.thread.service.ThreadService;
import com.sispro3d.unam.user.service.AccountService;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import com.sispro3d.unam.workorder.dto.WorkOrderResponse;
import com.sispro3d.unam.workorder.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detalle de una orden de trabajo, accesible solo para sus dos partes: muestra
 * estado, monto acordado, entregables con sus previsualizaciones y el hilo de
 * conversación. Las acciones permitidas por estado se ofrecen también aquí.
 */
@Controller
@RequestMapping("/orders")
@Slf4j
public class OrderDetailController {

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private OfferedServiceService offeredServiceService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private DeliverableService deliverableService;

    @Autowired
    private PreviewService previewService;

    @Autowired
    private ThreadService threadService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private CurrentUser currentUser;

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.debug("GET /orders/{} - usuario={}", id, currentUser.id());
        return loadDetail(id, model, new MessageRequest());
    }

    @PostMapping("/{id}/messages")
    public String postMessage(@PathVariable Long id,
                              @Valid @ModelAttribute("message") MessageRequest message,
                              BindingResult result,
                              Model model,
                              RedirectAttributes attributes) {
        log.debug("POST /orders/{}/messages - usuario={}", id, currentUser.id());
        requireParticipant(id);

        if (result.hasErrors()) {
            return loadDetail(id, model, message);
        }

        try {
            message.setUserId(currentUser.id());
            message.setThreadId(ensureThread(id).getId());
            messageService.create(message);
            attributes.addFlashAttribute("success", "Mensaje enviado.");
        } catch (RuntimeException ex) {
            attributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, RedirectAttributes attributes) {
        Long userId = currentUser.id();
        log.debug("POST /orders/{}/complete - usuario={}", id, userId);
        return run(id, attributes, () -> workOrderService.complete(id, userId), "Orden completada.");
    }

    @PostMapping("/{id}/request-changes")
    public String requestChanges(@PathVariable Long id, RedirectAttributes attributes) {
        Long userId = currentUser.id();
        log.debug("POST /orders/{}/request-changes - usuario={}", id, userId);
        return run(id, attributes, () -> workOrderService.requestChanges(id, userId),
                "Solicitaste cambios en el trabajo.");
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes attributes) {
        Long userId = currentUser.id();
        log.debug("POST /orders/{}/cancel - usuario={}", id, userId);
        return run(id, attributes, () -> workOrderService.cancel(id, userId), "Orden cancelada.");
    }

    @PostMapping("/{id}/start")
    public String start(@PathVariable Long id, RedirectAttributes attributes) {
        Long userId = currentUser.id();
        log.debug("POST /orders/{}/start - usuario={}", id, userId);
        return run(id, attributes, () -> workOrderService.start(id, userId), "Orden iniciada.");
    }

    @PostMapping("/{id}/mark-in-review")
    public String markInReview(@PathVariable Long id, RedirectAttributes attributes) {
        Long userId = currentUser.id();
        log.debug("POST /orders/{}/mark-in-review - usuario={}", id, userId);
        return run(id, attributes, () -> workOrderService.markInReview(id, userId),
                "Orden enviada a revisión del cliente.");
    }

    private String loadDetail(Long id, Model model, MessageRequest boundMessage) {
        WorkOrderResponse order = requireParticipant(id);
        Long userId = currentUser.id();

        OfferedServiceResponse service = offeredServiceService.findById(order.getOfferedServiceId())
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", order.getOfferedServiceId()));

        QuoteResponse quote = quoteService.findById(order.getQuoteId()).orElse(null);

        List<DeliverableResponse> deliverables = deliverableService.findByWorkOrderId(id);
        Map<Long, List<PreviewResponse>> previewsByDeliverable = new HashMap<>();
        for (DeliverableResponse deliverable : deliverables) {
            previewsByDeliverable.put(deliverable.getId(),
                    previewService.findByDeliverableId(deliverable.getId()));
        }

        ThreadResponse thread = ensureThread(id);
        List<MessageResponse> messages = messageService.findByThreadId(thread.getId());

        model.addAttribute("order", order);
        model.addAttribute("service", service);
        model.addAttribute("amount", quote != null ? quote.getTotalAmount() : null);
        model.addAttribute("clientName", accountService.findById(order.getClientId())
                .map(a -> a.getName() + " " + a.getLastName())
                .orElse("Cliente"));
        model.addAttribute("expertName", accountService.findById(service.getExpertId())
                .map(a -> a.getName() + " " + a.getLastName())
                .orElse("Experto"));
        model.addAttribute("deliverables", deliverables);
        model.addAttribute("previewsByDeliverable", previewsByDeliverable);
        model.addAttribute("thread", thread);
        model.addAttribute("messages", messages);
        model.addAttribute("authorNames", authorNames(messages));
        model.addAttribute("message", boundMessage);
        model.addAttribute("isClient", order.getClientId().equals(userId));
        model.addAttribute("isExpert", !order.getClientId().equals(userId));
        model.addAttribute("isConcluded", order.getStatus() == WorkOrderStatus.COMPLETED
                || order.getStatus() == WorkOrderStatus.CANCELED);
        return "orders/detail";
    }

    private WorkOrderResponse requireParticipant(Long id) {
        WorkOrderResponse order = workOrderService.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", id));

        Long userId = currentUser.id();

        boolean isClient = order.getClientId().equals(userId);
        boolean isExpert = offeredServiceService.findById(order.getOfferedServiceId())
                .map(service -> service.getExpertId().equals(userId))
                .orElse(false);

        if (!isClient && !isExpert) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada");
        }
        return order;
    }

    private ThreadResponse ensureThread(Long orderId) {
        return threadService.findByWorkOrderId(orderId)
                .orElseGet(() -> threadService.create(ThreadRequest.builder()
                        .actorId(currentUser.id())
                        .workOrderId(orderId)
                        .build()));
    }

    private Map<Long, String> authorNames(List<MessageResponse> messages) {
        Map<Long, String> names = new HashMap<>();
        for (MessageResponse message : messages) {
            names.computeIfAbsent(message.getUserId(), userId ->
                    accountService.findById(userId)
                            .map(a -> a.getName() + " " + a.getLastName())
                            .orElse("Usuario"));
        }
        return names;
    }

    private String run(Long id, RedirectAttributes attributes, Runnable transition, String successMessage) {
        try {
            transition.run();
            attributes.addFlashAttribute("success", successMessage);
        } catch (RuntimeException ex) {
            attributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }
}