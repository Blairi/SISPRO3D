package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceRequest;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.user.dto.AccountResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/expert")
public class ExpertDashboardController {

    @Autowired
    private OfferedServiceService offeredServiceService;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }

        var services = offeredServiceService.findByExpertId(userId);
        model.addAttribute("services", services);
        return "expert/dashboard";
    }

    @GetMapping("/services/new")
    public String showCreateForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("offeredservice", new OfferedServiceRequest());
        model.addAttribute("categories", categoryRepository.findAll());
        return "expert/create-service";
    }

    @PostMapping("/services/new")
    public String createService(
            @Valid @ModelAttribute("offeredservice") OfferedServiceRequest request,
            BindingResult result,
            HttpSession session,
            Model model) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return "expert/create-service";
        }

        request.setExpertId(userId);
        offeredServiceService.create(request);
        return "redirect:/expert/dashboard";
    }

    @GetMapping("/services/{id}/edit")
    public String showEditForm(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }

        offeredServiceService.findById(id)
                .filter(svc -> svc.getExpertId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        var response = offeredServiceService.findById(id).orElseThrow();
        OfferedServiceRequest request = OfferedServiceRequest.builder()
                .title(response.getTitle())
                .description(response.getDescription())
                .basePrice(response.getBasePrice())
                .categoryId(response.getCategoryId())
                .deliveryTimeDays(response.getDeliveryTimeDays())
                .build();

        model.addAttribute("offeredservice", request);
        model.addAttribute("serviceId", id);
        model.addAttribute("categories", categoryRepository.findAll());
        return "expert/edit-service";
    }

    @PostMapping("/services/{id}/edit")
    public String updateService(
            @PathVariable Long id,
            @Valid @ModelAttribute("offeredservice") OfferedServiceRequest request,
            BindingResult result,
            HttpSession session,
            Model model) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }

        if (!offeredServiceService.findById(id)
                .filter(svc -> svc.getExpertId().equals(userId))
                .isPresent()) {
            throw new IllegalArgumentException("Servicio no encontrado");
        }

        if (result.hasErrors()) {
            model.addAttribute("serviceId", id);
            model.addAttribute("categories", categoryRepository.findAll());
            return "expert/edit-service";
        }

        request.setExpertId(userId);
        offeredServiceService.update(id, request);
        return "redirect:/expert/dashboard";
    }

    @GetMapping("/services/{id}/delete")
    public String deleteService(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }

        offeredServiceService.findById(id)
                .filter(svc -> svc.getExpertId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        offeredServiceService.delete(id);
        return "redirect:/expert/dashboard";
    }
}
