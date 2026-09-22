package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ServiceCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OfferedServiceRepository offeredServiceRepository;

    private Long expertId;
    private Long adminId;
    private Category category;
    private Long pendingId;
    private Long rejectedId;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName("Modelado");
        category.setDescription("Servicios de modelado 3D");
        categoryRepository.save(category);

        Account expert = new Account();
        expert.setName("Lucia");
        expert.setLastName("Paredes");
        expert.setEmail("lucia@sec.test");
        expert.setPassword("$2a$10$6OFUT4kW8.Ix/obINbfLiO3bvcF.4BN4.AVcxQBIIQvYDwXhi0azm");
        expert.setRole(Role.EXPERT);
        expert.setSpecialty("Arquitectura 3D");
        expert.setYearsExperience(7);
        expert.setBio("Experta en modelado arquitectónico.");
        expert.setPortfolioUrl("https://portfolio.example/lucia");
        expert.setCreatedAt(LocalDateTime.now());
        expertId = accountRepository.save(expert).getIdUser();

        Account admin = new Account();
        admin.setName("Ricardo");
        admin.setLastName("Solano");
        admin.setEmail("ricardo@sec.test");
        admin.setPassword("$2a$10$3KduQCbjkdDMClf3EHZ3rOUwRD2sytH6h1Ayd.NrEYU1IfyJYpIim");
        admin.setRole(Role.ADMIN);
        admin.setCreatedAt(LocalDateTime.now());
        adminId = accountRepository.save(admin).getIdUser();

        pendingId = createService("Servicio pendiente", ServiceStatus.PENDING, LocalDateTime.now().minusDays(1));
        rejectedId = createService("Servicio rechazado", ServiceStatus.REJECTED, LocalDateTime.now().minusDays(2));
    }

    private Long createService(String title, ServiceStatus status, LocalDateTime createdAt) {
        OfferedService service = new OfferedService();
        service.setTitle(title);
        service.setDescription("Descripción de " + title);
        service.setBasePrice(new BigDecimal("1200.00"));
        service.setDeliveryTimeDays(10);
        service.setExpert(accountRepository.getReferenceById(expertId));
        service.setCategory(category);
        service.setAdmin(status == ServiceStatus.PENDING ? null : accountRepository.getReferenceById(adminId));
        service.setStatus(status);
        service.setCreatedAt(createdAt);
        service.setUpdatedAt(createdAt);
        return offeredServiceRepository.save(service).getId();
    }

    @Test
    void catalogEmpty_whenNoApprovedServices() throws Exception {
        mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No hay servicios disponibles")))
                .andExpect(content().string(not(containsString("Servicio pendiente"))))
                .andExpect(content().string(not(containsString("Servicio rechazado"))));
    }

    @Test
    void catalogListsOnlyApprovedServices_anonymously() throws Exception {
        createService("Modelado aprobado", ServiceStatus.APPROVED, LocalDateTime.now());

        mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Modelado aprobado")))
                .andExpect(content().string(containsString("Lucia Paredes")))
                .andExpect(content().string(containsString("$1200.00")))
                .andExpect(content().string(containsString("Modelado")))
                .andExpect(content().string(containsString("10 días de entrega")))
                .andExpect(content().string(not(containsString("Servicio pendiente"))))
                .andExpect(content().string(not(containsString("Servicio rechazado"))));
    }

    @Test
    void catalogOrdersApprovedServicesByMostRecent() throws Exception {
        createService("Aprobado viejo", ServiceStatus.APPROVED, LocalDateTime.now().minusDays(10));
        createService("Aprobado nuevo", ServiceStatus.APPROVED, LocalDateTime.now());

        String body = mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertTrue(
                body.indexOf("Aprobado nuevo") < body.indexOf("Aprobado viejo"),
                "El catálogo debe ordenar de más reciente a más antiguo");
    }

    @Test
    void detailShowsApprovedServiceAndExpertCard() throws Exception {
        Long approvedId = createService("Diseño de muebles", ServiceStatus.APPROVED, LocalDateTime.now());

        mockMvc.perform(get("/services/" + approvedId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Diseño de muebles")))
                .andExpect(content().string(containsString("Descripción de Diseño de muebles")))
                .andExpect(content().string(containsString("$1200.00")))
                .andExpect(content().string(containsString("Lucia Paredes")))
                .andExpect(content().string(containsString("Arquitectura 3D")))
                .andExpect(content().string(containsString("7 años")))
                .andExpect(content().string(containsString("Experta en modelado arquitectónico.")))
                .andExpect(content().string(containsString("https://portfolio.example/lucia")))
                .andExpect(content().string(containsString("Volver al catálogo")));
    }

    @Test
    void detailOfPendingService_isNotFound() throws Exception {
        mockMvc.perform(get("/services/" + pendingId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(not(containsString("Servicio pendiente"))));
    }

    @Test
    void detailOfRejectedService_isNotFound() throws Exception {
        mockMvc.perform(get("/services/" + rejectedId))
                .andExpect(status().isNotFound());
    }

    @Test
    void detailOfInexistentService_isNotFound() throws Exception {
        mockMvc.perform(get("/services/999999"))
                .andExpect(status().isNotFound());
    }
}