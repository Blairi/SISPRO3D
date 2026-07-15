package com.sispro3d.unam.offeredservice.service.impl;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceRequest;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OfferedServiceServiceImplTest {

    @Autowired
    private OfferedServiceService offeredServiceService;

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

    @Test
    void create() {
        var expert = createExpert("expert.create@sispro3d.com");
        var category = createCategory("Modelado");

        var request = OfferedServiceRequest.builder()
                .title("Modelado de personaje")
                .description("Servicio de modelado 3D")
                .basePrice(new BigDecimal("2000.00"))
                .expertId(expert.getIdUser())
                .categoryId(category.getId())
                .status(ServiceStatus.PENDING)
                .deliveryTimeDays(10)
                .build();

        OfferedServiceResponse res = offeredServiceService.create(request);

        assertThat(res.getId()).isNotNull();
        assertThat(res.getTitle()).isEqualTo("Modelado de personaje");
        assertThat(res.getExpertId()).isEqualTo(expert.getIdUser());
        assertThat(res.getCategoryId()).isEqualTo(category.getId());
        assertThat(res.getStatus()).isEqualTo(ServiceStatus.PENDING);
    }

    @Test
    void findById_whenExists() {
        var expert = createExpert("expert.find@sispro3d.com");
        var category = createCategory("Texturizado");

        var request = OfferedServiceRequest.builder()
                .title("Texturizado PBR")
                .description("Texturizado realista")
                .basePrice(new BigDecimal("1500.00"))
                .expertId(expert.getIdUser())
                .categoryId(category.getId())
                .status(ServiceStatus.APPROVED)
                .deliveryTimeDays(5)
                .build();
        OfferedServiceResponse created = offeredServiceService.create(request);

        Optional<OfferedServiceResponse> res = offeredServiceService.findById(created.getId());

        assertThat(res).isPresent();
        assertThat(res.get().getTitle()).isEqualTo("Texturizado PBR");
    }

    @Test
    void findById_whenNotExists() {
        Optional<OfferedServiceResponse> res = offeredServiceService.findById(999L);
        assertThat(res).isEmpty();
    }

    @Test
    void findAll() {
        var expert = createExpert("expert.findAll@sispro3d.com");
        var category = createCategory("Animacion");

        var request = OfferedServiceRequest.builder()
                .title("Animacion de personaje")
                .description("Animacion completa")
                .basePrice(new BigDecimal("3000.00"))
                .expertId(expert.getIdUser())
                .categoryId(category.getId())
                .status(ServiceStatus.PENDING)
                .deliveryTimeDays(14)
                .build();
        offeredServiceService.create(request);

        List<OfferedServiceResponse> res = offeredServiceService.findAll();
        assertThat(res).isNotEmpty();
    }

    @Test
    void update_whenExists() {
        var expert = createExpert("expert.update@sispro3d.com");
        var category = createCategory("Rigging");

        var createRequest = OfferedServiceRequest.builder()
                .title("Rigging basico")
                .description("Rigging de personaje")
                .basePrice(new BigDecimal("1000.00"))
                .expertId(expert.getIdUser())
                .categoryId(category.getId())
                .status(ServiceStatus.PENDING)
                .deliveryTimeDays(7)
                .build();
        OfferedServiceResponse created = offeredServiceService.create(createRequest);

        var updateRequest = OfferedServiceRequest.builder()
                .title("Rigging avanzado")
                .description("Rigging completo con facial")
                .basePrice(new BigDecimal("2500.00"))
                .expertId(expert.getIdUser())
                .categoryId(category.getId())
                .status(ServiceStatus.APPROVED)
                .deliveryTimeDays(14)
                .build();
        OfferedServiceResponse updated = offeredServiceService.update(created.getId(), updateRequest);

        assertThat(updated.getTitle()).isEqualTo("Rigging avanzado");
        assertThat(updated.getStatus()).isEqualTo(ServiceStatus.APPROVED);
    }

    @Test
    void update_whenNotExists() {
        var request = OfferedServiceRequest.builder()
                .title("No existe")
                .build();

        assertThatThrownBy(() -> offeredServiceService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists() {
        var expert = createExpert("expert.delete@sispro3d.com");
        var category = createCategory("Borrar");

        var request = OfferedServiceRequest.builder()
                .title("Para borrar")
                .description("Servicio temporal")
                .basePrice(new BigDecimal("500.00"))
                .expertId(expert.getIdUser())
                .categoryId(category.getId())
                .status(ServiceStatus.PENDING)
                .deliveryTimeDays(3)
                .build();
        OfferedServiceResponse created = offeredServiceService.create(request);

        offeredServiceService.delete(created.getId());

        assertThat(offeredServiceRepository.existsById(created.getId())).isFalse();
    }

    @Test
    void delete_whenNotExists() {
        assertThatThrownBy(() -> offeredServiceService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void existsById() {
        var expert = createExpert("expert.exists@sispro3d.com");
        var category = createCategory("Existe");

        var request = OfferedServiceRequest.builder()
                .title("Servicio existente")
                .description("Test")
                .basePrice(new BigDecimal("800.00"))
                .expertId(expert.getIdUser())
                .categoryId(category.getId())
                .status(ServiceStatus.PENDING)
                .deliveryTimeDays(5)
                .build();
        OfferedServiceResponse created = offeredServiceService.create(request);

        assertThat(offeredServiceService.existsById(created.getId())).isTrue();
    }
}
