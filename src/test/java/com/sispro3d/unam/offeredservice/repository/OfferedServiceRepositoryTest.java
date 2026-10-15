package com.sispro3d.unam.offeredservice.repository;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OfferedServiceRepositoryTest {

    @Autowired
    private OfferedServiceRepository offeredServiceRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Account createExpert(String email) {
        var account = new Account();
        account.setName("Expert");
        account.setLastName("Test");
        account.setEmail(email);
        account.setPassword("test123");
        account.setRole(Role.EXPERT);
        account.setCreatedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }

    private Category createCategory(String name) {
        var category = new Category();
        category.setName(name);
        category.setDescription("Test category");
        return categoryRepository.save(category);
    }

    private OfferedService createService(String title, Account expert, Category category) {
        var service = new OfferedService();
        service.setTitle(title);
        service.setDescription("Test description");
        service.setBasePrice(new BigDecimal("1500.00"));
        service.setExpert(expert);
        service.setCategory(category);
        service.setStatus(ServiceStatus.PENDING);
        service.setDeliveryTimeDays(7);
        service.setCreatedAt(LocalDateTime.now());
        service.setUpdatedAt(LocalDateTime.now());
        return offeredServiceRepository.save(service);
    }

    @Test
    void save() {
        var expert = createExpert("expert1@sispro3d.com");
        var category = createCategory("Modelado 3D");
        var service = createService("Modelado de personaje", expert, category);

        assertThat(service.getId()).isNotNull();
        assertThat(service.getTitle()).isEqualTo("Modelado de personaje");
        assertThat(service.getStatus()).isEqualTo(ServiceStatus.PENDING);
    }

    @Test
    void findByExpert() {
        var expert = createExpert("expert2@sispro3d.com");
        var category = createCategory("Texturizado");
        createService("Texturizado PBR", expert, category);

        List<OfferedService> result = offeredServiceRepository.findByExpert(expert);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(s -> s.getExpert().equals(expert));
    }

    @Test
    void findByCategory() {
        var expert = createExpert("expert3@sispro3d.com");
        var category = createCategory("Animacion");
        createService("Animacion de personaje", expert, category);

        List<OfferedService> result = offeredServiceRepository.findByCategory(category);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(s -> s.getCategory().equals(category));
    }

    @Test
    void findByStatus() {
        var expert = createExpert("expert4@sispro3d.com");
        var category = createCategory("Rigging");
        createService("Rigging completo", expert, category);

        List<OfferedService> result = offeredServiceRepository.findByStatus(ServiceStatus.PENDING);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(s -> s.getStatus() == ServiceStatus.PENDING);
    }
}
