package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.review.dto.ReviewRequest;
import com.sispro3d.unam.review.dto.ReviewResponse;
import com.sispro3d.unam.review.service.ReviewService;
import com.sispro3d.unam.security.SecurityAccount;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.dto.AccountResponse;
import com.sispro3d.unam.user.service.AccountService;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import com.sispro3d.unam.workorder.service.WorkOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

@Controller
@RequestMapping("/services")
public class ServiceCatalogController {

    @Autowired
    private OfferedServiceService offeredServiceService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private WorkOrderService workOrderService;

    @GetMapping
    public String index(Model model) {
        List<OfferedServiceResponse> services = offeredServiceService.findByStatus(ServiceStatus.APPROVED).stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .toList();
        model.addAttribute("services", services);
        return "services/index";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        OfferedServiceResponse service = offeredServiceService.findById(id)
                .filter(svc -> svc.getStatus() == ServiceStatus.APPROVED)
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", id));

        AccountResponse expert = accountService.findById(service.getExpertId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (expert)", service.getExpertId()));

        List<ReviewResponse> reviews = reviewService.findByOfferedServiceId(id);

        model.addAttribute("service", service);
        model.addAttribute("expert", expert);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewerNames", reviewerNames(reviews));
        model.addAttribute("avgRating", averageRating(reviews));
        model.addAttribute("avgStars", starCount(reviews));
        model.addAttribute("canReview", canReview(id));
        model.addAttribute("reviewRequest", new ReviewRequest());
        return "services/detail";
    }

    @PostMapping("/{id}/review")
    public String review(@PathVariable Long id,
                         @Valid @ModelAttribute("reviewRequest") ReviewRequest request,
                         BindingResult result,
                         RedirectAttributes attributes) {
        Long clientId = authenticatedUserId();
        if (clientId == null) {
            throw new AccessDeniedException("Debes iniciar sesión para publicar una reseña");
        }

        if (result.hasErrors()) {
            String message = result.getFieldErrors().stream()
                    .findFirst()
                    .map(err -> err.getDefaultMessage() == null ? "Datos inválidos" : err.getDefaultMessage())
                    .orElse("Datos inválidos");
            attributes.addFlashAttribute("reviewError", message);
            return "redirect:/services/" + id;
        }

        request.setClientId(clientId);
        request.setOfferedServiceId(id);

        try {
            reviewService.create(request);
            attributes.addFlashAttribute("reviewSuccess", "Reseña publicada. ¡Gracias!");
        } catch (RuntimeException ex) {
            attributes.addFlashAttribute("reviewError", ex.getMessage());
        }
        return "redirect:/services/" + id;
    }

    private Map<Long, String> reviewerNames(List<ReviewResponse> reviews) {
        Map<Long, String> names = new HashMap<>();
        for (ReviewResponse review : reviews) {
            names.computeIfAbsent(review.getClientId(), clientId ->
                    accountService.findById(clientId)
                            .map(a -> a.getName() + " " + a.getLastName())
                            .orElse("Cliente"));
        }
        return names;
    }

    private Double averageRating(List<ReviewResponse> reviews) {
        if (reviews.isEmpty()) {
            return null;
        }
        double average = reviews.stream()
                .mapToInt(ReviewResponse::getRating)
                .average()
                .orElse(0);
        return Math.round(average * 10) / 10.0;
    }

    private Integer starCount(List<ReviewResponse> reviews) {
        Double average = averageRating(reviews);
        if (average == null) {
            return null;
        }
        return (int) Math.round(average);
    }

    private boolean canReview(Long serviceId) {
        Long clientId = authenticatedUserId();
        if (clientId == null) {
            return false;
        }

        AccountResponse client = accountService.findById(clientId).orElse(null);
        if (client == null || client.getRole() != Role.CLIENT) {
            return false;
        }

        boolean hasCompletedOrder = workOrderService.findByClientId(clientId).stream()
                .anyMatch(order -> order.getOfferedServiceId().equals(serviceId)
                        && order.getStatus() == WorkOrderStatus.COMPLETED);
        boolean alreadyReviewed = reviewService.findByClientIdAndOfferedServiceId(clientId, serviceId).isPresent();

        return hasCompletedOrder && !alreadyReviewed;
    }

    private Long authenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SecurityAccount securityAccount) {
            return securityAccount.getId();
        }
        return null;
    }
}