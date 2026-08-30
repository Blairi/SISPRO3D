package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.user.repository.AccountRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/login")
public class LoginController {

    public static final String SESSION_USER_ID = "userId";

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping
    public String showForm() {
        return "login";
    }

    @PostMapping
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        return accountRepository.findByEmail(email)
                .filter(account -> account.getPassword().equals(password))
                .map(account -> {
                    session.setAttribute(SESSION_USER_ID, account.getIdUser());
                    return "redirect:/";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Correo o contraseña incorrectos");
                    model.addAttribute("email", email);
                    return "login";
                });
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
