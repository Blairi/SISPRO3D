package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.security.CurrentUser;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.dto.AccountRequest;
import com.sispro3d.unam.user.dto.AccountResponse;
import com.sispro3d.unam.user.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private OfferedServiceService offeredServiceService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CurrentUser currentUser;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Long userId = currentUser.id();

        List<OfferedServiceResponse> services = offeredServiceService.findAll().stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .toList();

        Map<String, List<OfferedServiceResponse>> grouped = new LinkedHashMap<>();
        for (OfferedServiceResponse svc : services) {
            String category = svc.getCategoryName() != null ? svc.getCategoryName() : "Sin categoría";
            grouped.computeIfAbsent(category, k -> new java.util.ArrayList<>()).add(svc);
        }

        List<AccountResponse> admins = accountService.findByRole(Role.ADMIN);

        model.addAttribute("groupedServices", grouped);
        model.addAttribute("admins", admins);
        return "admin/dashboard";
    }

    @GetMapping("/admins/new")
    public String showCreateAdminForm(Model model) {
        Long userId = currentUser.id();

        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new AccountRequest());
        }
        return "admin/create-admin";
    }

    @PostMapping("/admins/new")
    public String createAdmin(
            @Valid @ModelAttribute("user") AccountRequest request,
            BindingResult result) {
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            result.rejectValue("password", "acc.NotBlank.password", "La contraseña es obligatoria");
        } else if (request.getPassword().length() < 6) {
            result.rejectValue("password", "acc.Size.password", "La contraseña debe tener al menos 6 caracteres");
        }

        if (result.hasErrors()) {
            return "admin/create-admin";
        }

        accountService.create(request);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/services/{id}/approve")
    public String approve(@PathVariable Long id) {
        Long userId = currentUser.id();

        offeredServiceService.approve(id, userId);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/services/{id}/reject")
    public String reject(@PathVariable Long id) {
        Long userId = currentUser.id();

        offeredServiceService.reject(id, userId);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/services/{id}/pending")
    public String markPending(@PathVariable Long id) {
        Long userId = currentUser.id();

        offeredServiceService.markPending(id, userId);
        return "redirect:/admin/dashboard";
    }
}