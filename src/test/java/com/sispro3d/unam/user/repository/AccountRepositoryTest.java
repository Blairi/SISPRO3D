package com.sispro3d.unam.user.repository;

import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void save() {
        var account = new Account();
        account.setName("Demo");
        account.setLastName("Test");
        account.setEmail("demo@sispro3d.com");
        account.setPassword("demo123");
        account.setRole(Role.CLIENT);
        account.setCreatedAt(LocalDateTime.now());
        var saved = accountRepository.save(account);
        assertThat(saved.getIdUser()).isNotNull();
    }

    @Test
    void findByEmail() {
        var account = new Account();
        account.setName("Ana");
        account.setLastName("García");
        account.setEmail("ana@sispro3d.com");
        account.setPassword("clave123");
        account.setRole(Role.EXPERT);
        account.setCreatedAt(LocalDateTime.now());
        accountRepository.save(account);

        Optional<Account> res = accountRepository.findByEmail("ana@sispro3d.com");

        assertThat(res).isPresent();
        assertThat(res.get().getName()).isEqualTo("Ana");
    }

    @Test
    void findByRole() {
        var expert = new Account();
        expert.setName("Manager");
        expert.setLastName("One");
        expert.setEmail("manager@sispro3d.com");
        expert.setPassword("test123");
        expert.setRole(Role.EXPERT);
        expert.setCreatedAt(LocalDateTime.now());
        accountRepository.save(expert);

        List<Account> res = accountRepository.findByRole(Role.EXPERT);
        assertThat(res).isNotEmpty();
        assertThat(res).allMatch(a -> a.getRole() == Role.EXPERT);
    }

}