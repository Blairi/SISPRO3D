package com.sispro3d.unam.user.service.impl;

import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.dto.AccountRequest;
import com.sispro3d.unam.user.dto.AccountResponse;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.user.service.AccountService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public List<AccountResponse> findAll() {
        return accountRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Optional<AccountResponse> findById(Long id) {
        return accountRepository.findById(id.intValue())
                .map(this::toResponse);
    }

    @Override
    public AccountResponse create(AccountRequest request) {
        Account account = toEntity(request);
        Account saved = accountRepository.save(account);
        return toResponse(saved);
    }

    @Override
    public AccountResponse update(Long id, AccountRequest request) {
        int pk = id.intValue();
        accountRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Account no encontrado con id: " + id));

        Account account = toEntity(request);
        account.setIdUser(pk);
        Account updated = accountRepository.update(account);
        return toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        int pk = id.intValue();
        accountRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Account no encontrado con id: " + id));
        accountRepository.deleteById(pk);
    }

    @Override
    public boolean existsById(Long id) {
        return accountRepository.existsById(id.intValue());
    }

    private Account toEntity(AccountRequest request) {
        Account account = new Account();
        account.setName(request.getName());
        account.setLastName(request.getLastName());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setPassword(request.getPassword());
        account.setType(request.getType());
        return account;
    }

    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .idUser(account.getIdUser())
                .name(account.getName())
                .lastName(account.getLastName())
                .email(account.getEmail())
                .phone(account.getPhone())
                .password(account.getPassword())
                .type(account.getType())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
