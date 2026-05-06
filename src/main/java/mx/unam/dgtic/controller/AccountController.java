package mx.unam.dgtic.controller;

import mx.unam.dgtic.dao.AccountJdbcDAO;
import mx.unam.dgtic.dto.AccountDTO;
import mx.unam.dgtic.service.AccountService;
import mx.unam.dgtic.service.impl.AccountServiceImpl;

import java.util.List;
import java.util.Optional;

public class AccountController {
    private AccountService accountService;

    public AccountController() {
        this.accountService = new AccountServiceImpl(new AccountJdbcDAO());
    }

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
