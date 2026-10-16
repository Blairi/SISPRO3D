package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.security.CurrentUser;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.dto.AccountRequest;
import com.sispro3d.unam.user.dto.AccountResponse;
import com.sispro3d.unam.user.service.AccountService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@Slf4j
public class AdminUsersController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private CurrentUser currentUser;

    @GetMapping
    public String list(Model model) {
        Long userId = currentUser.id();
        log.debug("GET /admin/users - usuario={}", userId);

        List<AccountResponse> clients = accountService.findByRole(Role.CLIENT);
        List<AccountResponse> experts = accountService.findByRole(Role.EXPERT);

        model.addAttribute("clients", clients);
        model.addAttribute("experts", experts);
        return "admin/users";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        log.debug("GET /admin/users/{}/edit - usuario={}", id, currentUser.id());
        var account = accountService.findById(id).orElseThrow();

        AccountRequest request = AccountRequest.builder()
                .name(account.getName())
                .lastName(account.getLastName())
                .email(account.getEmail())
                .phone(account.getPhone())
                .role(account.getRole())
                .specialty(account.getSpecialty())
                .portfolioUrl(account.getPortfolioUrl())
                .bio(account.getBio())
                .yearsExperience(account.getYearsExperience())
                .build();

        model.addAttribute("user", request);
        model.addAttribute("userId", id);
        return "admin/user-form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("user") AccountRequest request, BindingResult result, Model model) {
        Long userId = currentUser.id();
        log.debug("POST /admin/users/{}/edit - usuario={}", id, userId);

        if (request.getPassword() != null && !request.getPassword().isBlank()
                && request.getPassword().length() < 6) {
            result.rejectValue("password", "acc.Size.password", "La contraseña debe tener al menos 6 caracteres");
        }

        if (result.hasErrors()) {
            model.addAttribute("userId", id);
            return "admin/user-form";
        }

        accountService.update(id, request);
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        Long userId = currentUser.id();
        log.debug("GET /admin/users/{}/delete - usuario={}", id, userId);

        accountService.delete(id);
        return "redirect:/admin/users";
    }
}