package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.security.SecurityAccount;
import com.sispro3d.unam.user.mapper.AccountMapper;
import com.sispro3d.unam.user.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SessionHelper {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountMapper accountMapper;

    @ModelAttribute
    public void addSessionUser(Authentication authentication, Model model) {
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityAccount securityAccount)) {
            return;
        }
        accountRepository.findById(securityAccount.getId())
                .map(accountMapper::toResponse)
                .ifPresent(user -> model.addAttribute("sessionUser", user));
    }
}