package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.dto.AccountRequest;
import com.sispro3d.unam.user.dto.AccountResponse;
import com.sispro3d.unam.user.service.AccountService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUsersController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public String list(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }

        List<AccountResponse> clients = accountService.findByRole(Role.CLIENT);
        List<AccountResponse> experts = accountService.findByRole(Role.EXPERT);

        model.addAttribute("clients", clients);
        model.addAttribute("experts", experts);
        return "admin/users";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }

        var response = accountService.findById(id).orElseThrow();
        model.addAttribute("user", response);
        return "admin/user-form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("user") AccountRequest request, BindingResult result, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }

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
    public String delete(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }

        accountService.delete(id);
        return "redirect:/admin/users";
    }
}
