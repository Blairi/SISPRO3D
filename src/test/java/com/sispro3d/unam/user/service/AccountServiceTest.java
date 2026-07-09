package com.sispro3d.unam.user.service;

import com.sispro3d.unam.user.domain.UserType;
import com.sispro3d.unam.user.dto.AccountRequest;
import com.sispro3d.unam.user.dto.AccountResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    private AccountResponse created;

    @BeforeEach
    void setUp() {
        created = accountService.create(AccountRequest.builder()
                .name("Test-Axel")
                .lastName("Test-Montiel")
                .email("test-axel@unam.mx")
                .phone("+52 55555555")
                .password("test-password")
                .type(UserType.ADMIN)
                .build());
    }

    @AfterEach
    void tearDown() {
        try {
            accountService.delete((long) created.getIdUser());
        } catch (Exception ignored) {
        }
    }

    @Test
    void create_shouldPersistAndReturnResponse() {
        assertThat(created.getIdUser()).isPositive();
        assertThat(created.getName()).isEqualTo("Test-Axel");
        assertThat(created.getEmail()).isEqualTo("test-axel@unam.mx");
    }

    @Test
    void findById_shouldReturnCreatedAccount() {
        var found = accountService.findById((long) created.getIdUser());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test-Axel");
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        var result = accountService.findById(99999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldContainCreatedAccount() {
        List<AccountResponse> all = accountService.findAll();

        assertThat(all)
                .filteredOn(a -> a.getIdUser() == created.getIdUser())
                .singleElement()
                .matches(a -> a.getName().equals("Test-Axel"));
    }

    @Test
    void update_shouldModifyAccount() {
        var updated = accountService.update((long) created.getIdUser(),
                AccountRequest.builder()
                        .name("Test-Actualizado")
                        .lastName(created.getLastName())
                        .email("test-actualizado@unam.mx")
                        .phone(created.getPhone())
                        .password(created.getPassword())
                        .type(created.getType())
                        .build());

        assertThat(updated.getName()).isEqualTo("Test-Actualizado");
        assertThat(updated.getEmail()).isEqualTo("test-actualizado@unam.mx");

        var reloaded = accountService.findById((long) created.getIdUser());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getName()).isEqualTo("Test-Actualizado");
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> accountService.update(99999L,
                AccountRequest.builder().name("X").build()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    void delete_shouldRemoveAccount() {
        accountService.delete((long) created.getIdUser());

        assertThat(accountService.findById((long) created.getIdUser())).isEmpty();
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> accountService.delete(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    void existsById_shouldReturnTrueForExisting() {
        assertThat(accountService.existsById((long) created.getIdUser())).isTrue();
    }

    @Test
    void existsById_shouldReturnFalseForNonExisting() {
        assertThat(accountService.existsById(99999L)).isFalse();
    }
}
