package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.category.dto.CategoryResponse;
import com.sispro3d.unam.category.service.CategoryService;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.dto.ServiceSearchCriteria;
import com.sispro3d.unam.offeredservice.dto.ServiceSortOrder;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/services")
public class ServiceCatalogController {

    @Autowired
    private OfferedServiceService offeredServiceService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private WorkOrderService workOrderService;

    @GetMapping
    public String index(@RequestParam(required = false) String q,
                        @RequestParam(required = false) String categoryId,
                        @RequestParam(required = false) String minPrice,
                        @RequestParam(required = false) String maxPrice,
                        @RequestParam(required = false) String maxDeliveryDays,
                        @RequestParam(required = false) String sort,
                        Model model) {
        List<String> filterErrors = new ArrayList<>();

        Long parsedCategoryId = parseLong(categoryId, filterErrors, "La categoría indicada no es válida.");
        BigDecimal parsedMinPrice = parseDecimal(minPrice, filterErrors, "El precio mínimo indicado no es un número válido.");
        BigDecimal parsedMaxPrice = parseDecimal(maxPrice, filterErrors, "El precio máximo indicado no es un número válido.");
        Integer parsedMaxDeliveryDays = parseInteger(maxDeliveryDays, filterErrors, "El tiempo de entrega indicado no es un número válido.");

        if (parsedMinPrice != null && parsedMaxPrice != null && parsedMinPrice.compareTo(parsedMaxPrice) > 0) {
            filterErrors.add("El precio mínimo no puede ser mayor que el precio máximo.");
            parsedMinPrice = null;
            parsedMaxPrice = null;
        }

        String query = q == null ? null : q.trim();
        if (query != null && query.isEmpty()) {
            query = null;
        }

        ServiceSortOrder sortOrder = parseSort(sort);

        List<OfferedServiceResponse> services = offeredServiceService.search(
                new ServiceSearchCriteria(query, parsedCategoryId, parsedMinPrice, parsedMaxPrice, parsedMaxDeliveryDays, sortOrder));

        boolean filtersActive = query != null
                || parsedCategoryId != null
                || parsedMinPrice != null
                || parsedMaxPrice != null
                || parsedMaxDeliveryDays != null;

        List<CategoryResponse> categories = categoryService.findAll();

        model.addAttribute("services", services);
        model.addAttribute("categories", categories);
        model.addAttribute("filterQuery", query);
        model.addAttribute("filterCategoryId", parsedCategoryId);
        model.addAttribute("filterMinPrice", parsedMinPrice);
        model.addAttribute("filterMaxPrice", parsedMaxPrice);
        model.addAttribute("filterMaxDeliveryDays", parsedMaxDeliveryDays);
        model.addAttribute("filterSort", sortOrder);
        model.addAttribute("filterErrors", filterErrors);
        model.addAttribute("filtersActive", filtersActive);
        model.addAttribute("hasApprovedServices", !offeredServiceService.findByStatus(ServiceStatus.APPROVED).isEmpty());
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

    private Long parseLong(String value, List<String> errors, String message) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            errors.add(message);
            return null;
        }
    }

    private BigDecimal parseDecimal(String value, List<String> errors, String message) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            errors.add(message);
            return null;
        }
    }

    private Integer parseInteger(String value, List<String> errors, String message) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            errors.add(message);
            return null;
        }
    }

    private ServiceSortOrder parseSort(String value) {
        if (value == null || value.isBlank()) {
            return ServiceSortOrder.NEWEST;
        }
        try {
            return ServiceSortOrder.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return ServiceSortOrder.NEWEST;
        }
    }
}