package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.deliverable.dto.DeliverableRequest;
import com.sispro3d.unam.deliverable.dto.DeliverableResponse;
import com.sispro3d.unam.deliverable.service.DeliverableService;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.preview.dto.PreviewRequest;
import com.sispro3d.unam.preview.dto.PreviewResponse;
import com.sispro3d.unam.preview.service.PreviewService;
import com.sispro3d.unam.quote.dto.QuoteAnswerForm;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.dto.QuoteResponse;
import com.sispro3d.unam.quote.domain.QuoteStatus;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.security.CurrentUser;
import com.sispro3d.unam.user.dto.AccountResponse;
import com.sispro3d.unam.user.service.AccountService;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import com.sispro3d.unam.workorder.dto.WorkOrderResponse;
import com.sispro3d.unam.workorder.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lado del experto del flujo de contratación: cotizaciones dirigidas a sus
 * servicios, órdenes de trabajo a su cargo y registro de entregables y
 * previsualizaciones. La ruta /expert/** está restringida al rol EXPERT y el
 * dominio vuelve a validar propiedad y fase en cada operación.
 */
@Controller
@RequestMapping("/expert")
@Slf4j
public class ExpertContractingController {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private DeliverableService deliverableService;

    @Autowired
    private PreviewService previewService;

    @Autowired
    private OfferedServiceService offeredServiceService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CurrentUser currentUser;

    @GetMapping("/quotes")
    public String quotes(Model model) {
        Long userId = currentUser.id();
        log.debug("GET /expert/quotes - usuario={}", userId);

        List<QuoteResponse> quotes = quoteService.findByExpertId(userId);

        model.addAttribute("pendingQuotes", quotes.stream()
                .filter(q -> q.getStatus() == QuoteStatus.PENDING).toList());
        model.addAttribute("answeredQuotes", quotes.stream()
                .filter(q -> q.getStatus() != QuoteStatus.PENDING).toList());
        model.addAttribute("serviceTitles", resolveTitles(quotes.stream().map(QuoteResponse::getOfferedServiceId).toList()));
        model.addAttribute("clientNames", resolveClientNames(quotes.stream().map(QuoteResponse::getClientId).toList()));
        return "expert/quotes";
    }

    @GetMapping("/quotes/{id}/answer")
    public String showAnswerForm(@PathVariable Long id, Model model, RedirectAttributes attributes) {
        Long userId = currentUser.id();
        log.debug("GET /expert/quotes/{}/answer - usuario={}", id, userId);

        Optional<QuoteResponse> quote = quoteService.findById(id);
        if (quote.isEmpty() || !isOwnQuote(userId, quote.get())) {
            attributes.addFlashAttribute("error", "No puedes responder esa cotización");
            return "redirect:/expert/quotes";
        }
        if (quote.get().getStatus() != QuoteStatus.PENDING) {
            attributes.addFlashAttribute("error", "La cotización ya fue resuelta");
            return "redirect:/expert/quotes";
        }

        prepareAnswerModel(model, quote.get());
        model.addAttribute("answer", new QuoteAnswerForm());
        return "expert/quote-answer";
    }

    @PostMapping("/quotes/{id}/answer")
    public String answer(@PathVariable Long id,
                         @Valid @ModelAttribute("answer") QuoteAnswerForm answer,
                         BindingResult result,
                         Model model,
                         RedirectAttributes attributes) {
        Long userId = currentUser.id();
        log.debug("POST /expert/quotes/{}/answer - usuario={}", id, userId);

        QuoteResponse quote = quoteService.findById(id).orElse(null);
        if (quote == null || !isOwnQuote(userId, quote)) {
            attributes.addFlashAttribute("error", "No puedes responder esa cotización");
            return "redirect:/expert/quotes";
        }

        if (result.hasErrors()) {
            prepareAnswerModel(model, quote);
            model.addAttribute("answer", answer);
            return "expert/quote-answer";
        }

        try {
            QuoteRequest request = QuoteRequest.builder()
                    .totalAmount(answer.getTotalAmount())
                    .validUntil(answer.getValidUntil())
                    .build();
            quoteService.respond(id, userId, request);
            attributes.addFlashAttribute("success", "Cotización respondida. Espera la decisión del cliente.");
        } catch (RuntimeException ex) {
            attributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/expert/quotes";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        Long userId = currentUser.id();
        log.debug("GET /expert/orders - usuario={}", userId);

        List<WorkOrderResponse> orders = workOrderService.findByExpertId(userId);

        model.addAttribute("activeOrders", orders.stream()
                .filter(o -> o.getStatus() == WorkOrderStatus.PENDING
                        || o.getStatus() == WorkOrderStatus.IN_PROGRESS
                        || o.getStatus() == WorkOrderStatus.IN_REVIEW).toList());
        model.addAttribute("finishedOrders", orders.stream()
                .filter(o -> o.getStatus() == WorkOrderStatus.COMPLETED
                        || o.getStatus() == WorkOrderStatus.CANCELED).toList());
        model.addAttribute("serviceTitles", resolveTitles(orders.stream().map(WorkOrderResponse::getOfferedServiceId).toList()));
        model.addAttribute("clientNames", resolveClientNames(orders.stream().map(WorkOrderResponse::getClientId).toList()));
        return "expert/orders";
    }

    @PostMapping("/orders/{id}/start")
    public String start(@PathVariable Long id, RedirectAttributes attributes) {
        Long userId = currentUser.id();
        log.debug("POST /expert/orders/{}/start - usuario={}", id, userId);
        return runOrder(attributes, () -> workOrderService.start(id, userId), "Orden iniciada.");
    }

    @PostMapping("/orders/{id}/mark-in-review")
    public String markInReview(@PathVariable Long id, RedirectAttributes attributes) {
        Long userId = currentUser.id();
        log.debug("POST /expert/orders/{}/mark-in-review - usuario={}", id, userId);
        return runOrder(attributes, () -> workOrderService.markInReview(id, userId),
                "Orden enviada a revisión del cliente.");
    }

    @GetMapping("/orders/{orderId}/deliverables/new")
    public String showDeliverableForm(@PathVariable Long orderId, Model model, RedirectAttributes attributes) {
        Long userId = currentUser.id();
        log.debug("GET /expert/orders/{}/deliverables/new - usuario={}", orderId, userId);

        WorkOrderResponse order = requireActiveOrder(orderId, userId, attributes);
        if (order == null) {
            return "redirect:/expert/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("deliverable", new DeliverableRequest());
        return "expert/deliverable-new";
    }

    @PostMapping("/orders/{orderId}/deliverables")
    public String registerDeliverable(@PathVariable Long orderId,
                                      @Valid @ModelAttribute("deliverable") DeliverableRequest deliverable,
                                      BindingResult result,
                                      Model model,
                                      RedirectAttributes attributes) {
        Long userId = currentUser.id();
        log.debug("POST /expert/orders/{}/deliverables - usuario={}", orderId, userId);

        WorkOrderResponse order = requireActiveOrder(orderId, userId, attributes);
        if (order == null) {
            return "redirect:/expert/orders";
        }

        if (result.hasErrors()) {
            model.addAttribute("order", order);
            model.addAttribute("deliverable", deliverable);
            return "expert/deliverable-new";
        }

        try {
            deliverable.setWorkOrderId(orderId);
            deliverable.setExpertId(userId);
            deliverableService.create(deliverable);
            attributes.addFlashAttribute("success", "Entregable registrado en la orden #" + orderId + ".");
        } catch (RuntimeException ex) {
            attributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orders/" + orderId;
    }

    @GetMapping("/orders/{orderId}/deliverables/{deliverableId}/previews/new")
    public String showPreviewForm(@PathVariable Long orderId,
                                  @PathVariable Long deliverableId,
                                  Model model,
                                  RedirectAttributes attributes) {
        Long userId = currentUser.id();
        log.debug("GET /expert/orders/{}/deliverables/{}/previews/new - usuario={}", orderId, deliverableId, userId);

        WorkOrderResponse order = requireActiveOrder(orderId, userId, attributes);
        if (order == null) {
            return "redirect:/expert/orders";
        }

        DeliverableResponse deliverable = findDeliverableOfOrder(deliverableId, orderId);
        if (deliverable == null) {
            attributes.addFlashAttribute("error", "El entregable no pertenece a esa orden");
            return "redirect:/orders/" + orderId;
        }

        model.addAttribute("order", order);
        model.addAttribute("deliverable", deliverable);
        model.addAttribute("preview", new PreviewRequest());
        return "expert/preview-new";
    }

    @PostMapping("/orders/{orderId}/deliverables/{deliverableId}/previews")
    public String registerPreview(@PathVariable Long orderId,
                                  @PathVariable Long deliverableId,
                                  @Valid @ModelAttribute("preview") PreviewRequest preview,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes attributes) {
        Long userId = currentUser.id();
        log.debug("POST /expert/orders/{}/deliverables/{}/previews - usuario={}", orderId, deliverableId, userId);

        WorkOrderResponse order = requireActiveOrder(orderId, userId, attributes);
        if (order == null) {
            return "redirect:/expert/orders";
        }

        DeliverableResponse deliverable = findDeliverableOfOrder(deliverableId, orderId);
        if (deliverable == null) {
            attributes.addFlashAttribute("error", "El entregable no pertenece a esa orden");
            return "redirect:/orders/" + orderId;
        }

        if (result.hasErrors()) {
            model.addAttribute("order", order);
            model.addAttribute("deliverable", deliverable);
            model.addAttribute("preview", preview);
            return "expert/preview-new";
        }

        try {
            preview.setDeliverableId(deliverableId);
            preview.setExpertId(userId);
            previewService.create(preview);
            attributes.addFlashAttribute("success", "Previsualización añadida al entregable.");
        } catch (RuntimeException ex) {
            attributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orders/" + orderId;
    }

    @PostMapping("/orders/{orderId}/deliverables/{deliverableId}/delete")
    public String deleteDeliverable(@PathVariable Long orderId,
                                    @PathVariable Long deliverableId,
                                    RedirectAttributes attributes) {
        Long userId = currentUser.id();
        log.debug("POST /expert/orders/{}/deliverables/{}/delete - usuario={}", orderId, deliverableId, userId);

        if (requireActiveOrder(orderId, userId, attributes) == null) {
            return "redirect:/expert/orders";
        }

        if (findDeliverableOfOrder(deliverableId, orderId) == null) {
            attributes.addFlashAttribute("error", "El entregable no pertenece a esa orden");
            return "redirect:/orders/" + orderId;
        }

        try {
            deliverableService.delete(deliverableId);
            attributes.addFlashAttribute("success", "Entregable eliminado.");
        } catch (RuntimeException ex) {
            attributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orders/" + orderId;
    }

    @PostMapping("/orders/{orderId}/deliverables/{deliverableId}/previews/{previewId}/delete")
    public String deletePreview(@PathVariable Long orderId,
                                @PathVariable Long deliverableId,
                                @PathVariable Long previewId,
                                RedirectAttributes attributes) {
        Long userId = currentUser.id();
        log.debug("POST /expert/orders/{}/deliverables/{}/previews/{}/delete - usuario={}", orderId, deliverableId, previewId, userId);

        if (requireActiveOrder(orderId, userId, attributes) == null) {
            return "redirect:/expert/orders";
        }

        if (findDeliverableOfOrder(deliverableId, orderId) == null) {
            attributes.addFlashAttribute("error", "El entregable no pertenece a esa orden");
            return "redirect:/orders/" + orderId;
        }

        boolean belongs = previewService.findById(previewId)
                .map(PreviewResponse::getDeliverableId)
                .map(deliverableId::equals)
                .orElse(false);
        if (!belongs) {
            attributes.addFlashAttribute("error", "La previsualización no pertenece a ese entregable");
            return "redirect:/orders/" + orderId;
        }

        try {
            previewService.delete(previewId);
            attributes.addFlashAttribute("success", "Previsualización eliminada.");
        } catch (RuntimeException ex) {
            attributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orders/" + orderId;
    }

    private void prepareAnswerModel(Model model, QuoteResponse quote) {
        model.addAttribute("quote", quote);
        model.addAttribute("serviceTitle", offeredServiceService.findById(quote.getOfferedServiceId())
                .map(OfferedServiceResponse::getTitle)
                .orElse("Servicio"));
        model.addAttribute("clientName", accountService.findById(quote.getClientId())
                .map(a -> a.getName() + " " + a.getLastName())
                .orElse("Cliente"));
    }

    private boolean isOwnQuote(Long userId, QuoteResponse quote) {
        return offeredServiceService.findById(quote.getOfferedServiceId())
                .map(svc -> svc.getExpertId().equals(userId))
                .orElse(false);
    }

    private WorkOrderResponse requireActiveOrder(Long orderId, Long userId, RedirectAttributes attributes) {
        WorkOrderResponse order = workOrderService.findById(orderId).orElse(null);
        if (order == null || !isOwnOrder(userId, order)) {
            attributes.addFlashAttribute("error", "Solo puede gestionar la orden el experto propietario del servicio");
            return null;
        }
        if (order.getStatus() != WorkOrderStatus.IN_PROGRESS
                && order.getStatus() != WorkOrderStatus.IN_REVIEW) {
            attributes.addFlashAttribute("error",
                    "Solo se pueden registrar entregables en órdenes IN_PROGRESS o IN_REVIEW");
            return null;
        }
        return order;
    }

    private boolean isOwnOrder(Long userId, WorkOrderResponse order) {
        return offeredServiceService.findById(order.getOfferedServiceId())
                .map(svc -> svc.getExpertId().equals(userId))
                .orElse(false);
    }

    private DeliverableResponse findDeliverableOfOrder(Long deliverableId, Long orderId) {
        return deliverableService.findById(deliverableId)
                .filter(d -> d.getWorkOrderId().equals(orderId))
                .orElse(null);
    }

    private Map<Long, String> resolveTitles(List<Long> serviceIds) {
        Map<Long, String> titles = new HashMap<>();
        for (Long serviceId : serviceIds) {
            titles.computeIfAbsent(serviceId, id ->
                    offeredServiceService.findById(id)
                            .map(OfferedServiceResponse::getTitle)
                            .orElse("Servicio"));
        }
        return titles;
    }

    private Map<Long, String> resolveClientNames(List<Long> clientIds) {
        Map<Long, String> names = new HashMap<>();
        for (Long clientId : clientIds) {
            names.computeIfAbsent(clientId, id ->
                    accountService.findById(id)
                            .map(AccountResponse::getName)
                            .orElse("Cliente"));
        }
        return names;
    }

    private String runOrder(RedirectAttributes attributes, Runnable transition, String successMessage) {
        try {
            transition.run();
            attributes.addFlashAttribute("success", successMessage);
        } catch (RuntimeException ex) {
            attributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/expert/orders";
    }
}