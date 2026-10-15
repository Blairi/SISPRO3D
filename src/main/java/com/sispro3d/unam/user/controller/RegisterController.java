package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.dto.AccountRequest;
import com.sispro3d.unam.user.dto.RegisterRequest;
import com.sispro3d.unam.user.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public String showForm(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "register";
    }

    @PostMapping
    public String register(
            @Valid @ModelAttribute RegisterRequest registerRequest,
            BindingResult result,
            Model model) {

        if ("EXPERT".equals(registerRequest.getRole())) {
            if (registerRequest.getSpecialty() == null || registerRequest.getSpecialty().isBlank()) {
                result.rejectValue("specialty", "NotBlank.specialty", "La especialidad es obligatoria para expertos");
            }
        }

        if (result.hasErrors()) {
            return "register";
        }

        AccountRequest accountRequest = AccountRequest.builder()
                .name(registerRequest.getName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .phone(registerRequest.getPhone())
                .password(registerRequest.getPassword())
                .role(Role.valueOf(registerRequest.getRole()))
                .specialty(registerRequest.getSpecialty())
                .portfolioUrl(registerRequest.getPortfolioUrl())
                .bio(registerRequest.getBio())
                .yearsExperience(registerRequest.getYearsExperience())
                .build();

        accountService.create(accountRequest);

        return "redirect:/login";
    }
}
