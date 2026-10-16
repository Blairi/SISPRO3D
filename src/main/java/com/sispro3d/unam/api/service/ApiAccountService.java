package com.sispro3d.unam.api.service;

import com.sispro3d.unam.api.dto.AccountRequestDTO;
import com.sispro3d.unam.api.dto.AccountResponseDTO;
import com.sispro3d.unam.api.exception.DataIntegrityException;
import com.sispro3d.unam.api.mapper.ApiAccountMapper;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.quote.repository.QuoteRepository;
import com.sispro3d.unam.user.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ApiAccountService {

    private final AccountRepository accountRepository;
    private final OfferedServiceRepository offeredServiceRepository;
    private final QuoteRepository quoteRepository;
    private final ApiAccountMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public ApiAccountService(AccountRepository accountRepository,
                             OfferedServiceRepository offeredServiceRepository,
                             QuoteRepository quoteRepository,
                             ApiAccountMapper mapper,
                             PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.offeredServiceRepository = offeredServiceRepository;
        this.quoteRepository = quoteRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    public AccountResponseDTO findById(Long id) {
        return accountRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account", id));
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

    public AccountResponseDTO create(AccountRequestDTO request) {
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Rechazado: correo ya en uso (API): email={}", request.getEmail());
            throw new DataIntegrityException("Email already in use: " + request.getEmail());
        }
        var account = mapper.toEntity(request);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setCreatedAt(LocalDateTime.now());
        var saved = accountRepository.save(account);
        log.info("Cuenta creada (API): id={}, email={}, rol={}", saved.getIdUser(), saved.getEmail(), saved.getRole());
        return mapper.toResponse(saved);
    }

    public AccountResponseDTO update(Long id, AccountRequestDTO request) {
        var existing = accountRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account", id));

        accountRepository.findByEmail(request.getEmail())
                .filter(other -> !other.getIdUser().equals(id))
                .ifPresent(other -> {
                    log.warn("Rechazado: correo ya en uso en actualización (API): email={}", request.getEmail());
                    throw new DataIntegrityException("Email already in use: " + request.getEmail());
                });

        mapper.updateEntityFromRequest(request, existing);
        existing.setPassword(encodeIfChanged(request.getPassword(), existing.getPassword()));
        var updated = accountRepository.save(existing);
        log.info("Cuenta actualizada (API): id={}", updated.getIdUser());
        return mapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        var existing = accountRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account", id));

        if (!offeredServiceRepository.findByExpert_IdUser(id).isEmpty()) {
            log.warn("Rechazado: cuenta experta de servicios no eliminable (API): id={}", id);
            throw new DataIntegrityException("Account with id " + id
                    + " is the expert of offered services and cannot be deleted");
        }
        if (!quoteRepository.findByClient_IdUser(id).isEmpty()) {
            log.warn("Rechazado: cuenta con cotizaciones no eliminable (API): id={}", id);
            throw new DataIntegrityException("Account with id " + id
                    + " has associated quotes and cannot be deleted");
        }
        accountRepository.delete(existing);
        log.info("Cuenta eliminada (API): id={}", id);
    }
}
