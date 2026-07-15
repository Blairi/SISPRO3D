package com.sispro3d.unam.user.service.impl;

import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.dto.AccountRequest;
import com.sispro3d.unam.user.dto.AccountResponse;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.user.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AccountServiceImplTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void create() {
        var request = new AccountRequest();
        request.setName("Demo");
        request.setLastName("Test");
        request.setEmail("demo@sispro3d.com");
        request.setPassword("demo123");
        request.setRole(Role.CLIENT);

        AccountResponse res = accountService.create(request);

        assertThat(res.getIdUser()).isNotNull();
        assertThat(res.getEmail()).isEqualTo("demo@sispro3d.com");
    }

    @Test
    void findById_whenExists() {
        var account = new Account();
        account.setName("Ana");
        account.setLastName("García");
        account.setEmail("ana@sispro3d.com");
        account.setPassword("clave123");
        account.setRole(Role.EXPERT);
        account.setCreatedAt(LocalDateTime.now());
        var saved = accountRepository.save(account);

        Optional<AccountResponse> res = accountService.findById(saved.getIdUser());

        assertThat(res).isPresent();
        assertThat(res.get().getName()).isEqualTo("Ana");
    }

    @Test
    void findById_whenNotExists() {
        Optional<AccountResponse> res = accountService.findById(999L);
        assertThat(res).isEmpty();
    }

    @Test
    void findAll() {
        var account = new Account();
        account.setName("Manager");
        account.setLastName("One");
        account.setEmail("manager@sispro3d.com");
        account.setPassword("test123");
        account.setRole(Role.EXPERT);
        account.setCreatedAt(LocalDateTime.now());
        accountRepository.save(account);

        List<AccountResponse> res = accountService.findAll();

        assertThat(res).isNotEmpty();
    }

    @Test
    void update_whenExists() {
        var account = new Account();
        account.setName("Ana");
        account.setLastName("García");
        account.setEmail("ana@sispro3d.com");
        account.setPassword("clave123");
        account.setRole(Role.EXPERT);
        account.setCreatedAt(LocalDateTime.now());
        var saved = accountRepository.save(account);

        var request = new AccountRequest();
        request.setName("Ana Updated");
        request.setLastName("García");
        request.setEmail("ana@sispro3d.com");
        request.setPassword("clave123");
        request.setRole(Role.EXPERT);

        AccountResponse res = accountService.update(saved.getIdUser(), request);

        assertThat(res.getName()).isEqualTo("Ana Updated");
    }

    @Test
    void update_whenNotExists() {
        var request = new AccountRequest();
        request.setName("Nadie");

        assertThatThrownBy(() -> accountService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists() {
        var account = new Account();
        account.setName("Borrar");
        account.setLastName("Me");
        account.setEmail("borrar@sispro3d.com");
        account.setPassword("test123");
        account.setRole(Role.CLIENT);
        account.setCreatedAt(LocalDateTime.now());
        var saved = accountRepository.save(account);

        accountService.delete(saved.getIdUser());

        assertThat(accountRepository.existsById(saved.getIdUser())).isFalse();
    }

    @Test
    void delete_whenNotExists() {
        assertThatThrownBy(() -> accountService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void existsById() {
        var account = new Account();
        account.setName("Existe");
        account.setLastName("Si");
        account.setEmail("existe@sispro3d.com");
        account.setPassword("test123");
        account.setRole(Role.CLIENT);
        account.setCreatedAt(LocalDateTime.now());
        var saved = accountRepository.save(account);

        assertThat(accountService.existsById(saved.getIdUser())).isTrue();
    }

}