package mx.unam.dgtic.controller;

import mx.unam.dgtic.service.AccountService;
import mx.unam.dgtic.service.impl.AccountServiceImpl;

public class AccountController {
    private final AccountService accountService;

    public AccountController() {
        accountService = new AccountServiceImpl();
    }

    public void displayAccount(int id) {
        System.out.println(accountService.findById(id));
    }

}