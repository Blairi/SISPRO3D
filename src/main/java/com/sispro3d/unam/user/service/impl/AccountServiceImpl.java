package com.sispro3d.unam.user.service.impl;


import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.dto.AccountRequest;
import com.sispro3d.unam.user.dto.AccountResponse;
import com.sispro3d.unam.user.mapper.AccountMapper;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.user.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private OfferedServiceRepository offeredServiceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<AccountResponse> findAll() {
        return accountRepository.findAll().stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Override
    public Optional<AccountResponse> findById(Long id) {
        return accountRepository.findById(id)
                .map(accountMapper::toResponse);
    }

    @Override
    public AccountResponse create(AccountRequest request) {
        Account account = accountMapper.toEntity(request);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setCreatedAt(LocalDateTime.now());
        Account saved = accountRepository.save(account);
        return accountMapper.toResponse(saved);
    }

    @Override
    public AccountResponse update(Long id, AccountRequest request) {
        Account existing = accountRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account", id));

        accountMapper.updateEntityFromRequest(request, existing);
        existing.setPassword(encodeIfChanged(request.getPassword(), existing.getPassword()));
        Account updated = accountRepository.save(existing);
        return accountMapper.toResponse(updated);
    }

    private String encodeIfChanged(String rawPassword, String storedPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            return storedPassword;
        }
        if (storedPassword == null || !isBcrypt(storedPassword)
                || !passwordEncoder.matches(rawPassword, storedPassword)) {
            return passwordEncoder.encode(rawPassword);
        }
        return storedPassword;
    }

    private boolean isBcrypt(String hash) {
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account", id));

        offeredServiceRepository.deleteAll(offeredServiceRepository.findByExpert_IdUser(id));
        accountRepository.delete(account);
    }

    @Override
    public boolean existsById(Long id) {
        return accountRepository.existsById(id);
    }

    @Override
    public Optional<AccountResponse> findByEmail(String email) {
        return accountRepository.findByEmail(email)
                .map(accountMapper::toResponse);
    }

    @Override
    public List<AccountResponse> findByRole(Role role) {
        return accountRepository.findByRole(role).stream()
                .map(accountMapper::toResponse)
                .toList();
    }

}
