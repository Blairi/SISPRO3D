package com.sispro3d.unam.api.service;

import com.sispro3d.unam.api.dto.AccountRequestDTO;
import com.sispro3d.unam.api.dto.AccountResponseDTO;
import com.sispro3d.unam.api.exception.DataIntegrityException;
import com.sispro3d.unam.api.mapper.ApiAccountMapper;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.quote.repository.QuoteRepository;
import com.sispro3d.unam.user.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ApiAccountService {

    private final AccountRepository accountRepository;
    private final OfferedServiceRepository offeredServiceRepository;
    private final QuoteRepository quoteRepository;
    private final ApiAccountMapper mapper;

    public ApiAccountService(AccountRepository accountRepository,
                             OfferedServiceRepository offeredServiceRepository,
                             QuoteRepository quoteRepository,
                             ApiAccountMapper mapper) {
        this.accountRepository = accountRepository;
        this.offeredServiceRepository = offeredServiceRepository;
        this.quoteRepository = quoteRepository;
        this.mapper = mapper;
    }

    public AccountResponseDTO findById(Long id) {
        return accountRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account", id));
    }

    public AccountResponseDTO create(AccountRequestDTO request) {
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DataIntegrityException("Email already in use: " + request.getEmail());
        }
        var account = mapper.toEntity(request);
        account.setCreatedAt(LocalDateTime.now());
        return mapper.toResponse(accountRepository.save(account));
    }

    public AccountResponseDTO update(Long id, AccountRequestDTO request) {
        var existing = accountRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account", id));

        accountRepository.findByEmail(request.getEmail())
                .filter(other -> !other.getIdUser().equals(id))
                .ifPresent(other -> {
                    throw new DataIntegrityException("Email already in use: " + request.getEmail());
                });

        mapper.updateEntityFromRequest(request, existing);
        return mapper.toResponse(accountRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        var existing = accountRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account", id));

        if (!offeredServiceRepository.findByExpert_IdUser(id).isEmpty()) {
            throw new DataIntegrityException("Account with id " + id
                    + " is the expert of offered services and cannot be deleted");
        }
        if (!quoteRepository.findByClient_IdUser(id).isEmpty()) {
            throw new DataIntegrityException("Account with id " + id
                    + " has associated quotes and cannot be deleted");
        }
        accountRepository.delete(existing);
    }
}
