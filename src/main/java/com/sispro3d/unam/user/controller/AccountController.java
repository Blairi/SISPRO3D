package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.user.dto.AccountRequest;
import com.sispro3d.unam.user.dto.AccountResponse;
import com.sispro3d.unam.user.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class AccountController {

    @Autowired
    private AccountService accountService;

    public void displayAccount(long id) {
        System.out.println("Displaying account with id = " + id);
        Optional<AccountResponse> accountDTO = accountService.findById(id);
        System.out.println("accountDTO = " + accountDTO);
    }

    public void displayAllAccounts() {
        System.out.println("Displaying all accounts:");
        List<AccountResponse> accounts = accountService.findAll();
        accounts.forEach(System.out::println);
    }

    public Optional<AccountResponse> getAccount(long id) {
        return accountService.findById(id);
    }

    public List<AccountResponse> getAllAccounts() {
        return accountService.findAll();
    }

    public AccountResponse createAccount(AccountRequest request) {
        return accountService.create(request);
    }

    public AccountResponse updateAccount(long id, AccountRequest request) {
        return accountService.update(id, request);
    }

    public void deleteAccount(long id) {
        accountService.delete(id);
    }
}
