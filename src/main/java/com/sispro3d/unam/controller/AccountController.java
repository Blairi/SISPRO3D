package com.sispro3d.unam.controller;

import com.sispro3d.unam.dao.AccountJdbcDAO;
import com.sispro3d.unam.dto.AccountDTO;
import com.sispro3d.unam.service.AccountService;
import com.sispro3d.unam.service.impl.AccountServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class AccountController {

    @Autowired
    private AccountService accountService;

    public void displayAccount(int id) {
        System.out.println("Displaying account with id = " + id);
        Optional<AccountDTO> accountDTO = accountService.findById(id);
        System.out.println("accountDTO = " + accountDTO);
    }

    public void displayAllAccounts() {
        System.out.println("Displaying all accounts:");
        List<AccountDTO> accounts = accountService.findAll();
        accounts.forEach(System.out::println);
    }

    public Optional<AccountDTO> getAccount(int id) {
        return accountService.findById(id);
    }

    public List<AccountDTO> getAllAccounts() {
        return accountService.findAll();
    }

    public void createAccount(AccountDTO newAccount) {
        accountService.create(newAccount);
    }

    public void updateAccount(int id, AccountDTO accountDTO) {
        accountService.update(id, accountDTO);
    }

    public void deleteAccount(int id) {
        accountService.delete(id);
    }
}
