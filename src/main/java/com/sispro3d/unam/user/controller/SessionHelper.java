package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.dto.AccountResponse;
import com.sispro3d.unam.user.mapper.AccountMapper;
import com.sispro3d.unam.user.repository.AccountRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class SessionHelper {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountMapper accountMapper;

    @ModelAttribute
    public void addSessionUser(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId != null) {
            accountRepository.findById(userId)
                    .map(accountMapper::toResponse)
                    .ifPresent(user -> model.addAttribute("sessionUser", user));
        }
    }
}
