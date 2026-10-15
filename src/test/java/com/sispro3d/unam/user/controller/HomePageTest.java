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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class HomePageTest {

    private static final String PASSWORD = "test123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OfferedServiceRepository offeredServiceRepository;

    private Long categoryId;
    private Long approvedNewId;

    @BeforeEach
    void setUp() {
        Account expert = createAccount("Experto", "experto@home.test", Role.EXPERT);
        createAccount("Admin", "admin@home.test", Role.ADMIN);
        createAccount("Cliente", "cliente@home.test", Role.CLIENT);

        Category category = new Category();
        category.setName("Modelado");
        category.setDescription("Servicios de modelado 3D");
        categoryId = categoryRepository.save(category).getId();

        approvedNewId = createService("Render fotorrealista", ServiceStatus.APPROVED, expert, category, LocalDateTime.now());
        createService("Animación antigua", ServiceStatus.APPROVED, expert, category, LocalDateTime.now().minusDays(5));
        createService("Pendiente oculto", ServiceStatus.PENDING, expert, category, LocalDateTime.now());
        createService("Rechazado oculto", ServiceStatus.REJECTED, expert, category, LocalDateTime.now());
    }

    @Test
    void landingRendersAllPresentationSections() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Da vida a tus ideas en 3D")))
                .andExpect(content().string(containsString("Servicios destacados")))
                .andExpect(content().string(containsString("Explora por categoría")))
                .andExpect(content().string(containsString("Cómo funciona")))
                .andExpect(content().string(containsString("¿Listo para empezar tu proyecto?")));
    }

    @Test
    void featuredSectionShowsOnlyApprovedServices() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Render fotorrealista")))
                .andExpect(content().string(not(containsString("Pendiente oculto"))))
                .andExpect(content().string(not(containsString("Rechazado oculto"))));
    }

    @Test
    void featuredSectionOrdersMostRecentFirst() throws Exception {
        String body = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertTrue(
                body.indexOf("Render fotorrealista") < body.indexOf("Animación antigua"),
                "Los servicios destacados deben ir del más reciente al más antiguo");
    }

    @Test
    void featuredCardLinksToServiceDetail() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/services/" + approvedNewId)));
    }

    @Test
    void categoriesSectionLinksToFilteredCatalog() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/services?categoryId=" + categoryId)));
    }

    @Test
    void landingUsesOnlyLocalAssets() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/images/pexels-")))
                .andExpect(content().string(not(containsString("http://"))))
                .andExpect(content().string(not(containsString("https://"))));
    }

    @Test
    void anonymousVisitorSeesOnlyPublicNavigation() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Servicios")))
                .andExpect(content().string(containsString("Iniciar sesión")))
                .andExpect(content().string(containsString("Registro")))
                .andExpect(content().string(not(containsString("Menú"))))
                .andExpect(content().string(not(containsString("Cerrar sesión"))));
    }

    @Test
    void clientNavigationShowsOnlyClientAreas() throws Exception {
        mockMvc.perform(get("/").session(login("cliente@home.test")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mis cotizaciones")))
                .andExpect(content().string(containsString("Mis órdenes")))
                .andExpect(content().string(containsString("Cerrar sesión")))
                .andExpect(content().string(not(containsString("Dashboard"))))
                .andExpect(content().string(not(containsString("Reportes"))))
                .andExpect(content().string(not(containsString("Iniciar sesión"))));
    }

    @Test
    void expertNavigationShowsOnlyExpertAreas() throws Exception {
        mockMvc.perform(get("/").session(login("experto@home.test")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cotizaciones")))
                .andExpect(content().string(containsString("Órdenes")))
                .andExpect(content().string(containsString("Cerrar sesión")))
                .andExpect(content().string(not(containsString("Mis cotizaciones"))))
                .andExpect(content().string(not(containsString("Dashboard"))))
                .andExpect(content().string(not(containsString("Iniciar sesión"))));
    }

    @Test
    void adminNavigationShowsOnlyAdministrativeAreas() throws Exception {
        mockMvc.perform(get("/").session(login("admin@home.test")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Dashboard")))
                .andExpect(content().string(containsString("Categorías")))
                .andExpect(content().string(containsString("Usuarios")))
                .andExpect(content().string(containsString("Reportes")))
                .andExpect(content().string(containsString("Cerrar sesión")))
                .andExpect(content().string(not(containsString("Mis cotizaciones"))))
                .andExpect(content().string(not(containsString("Iniciar sesión"))));
    }

    @Test
    void accountNameAndSignOutStayTogether() throws Exception {
        String body = mockMvc.perform(get("/").session(login("cliente@home.test")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cliente Test")))
                .andExpect(content().string(containsString("Cerrar sesión")))
                .andReturn().getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertTrue(
                body.indexOf("Cliente Test") < body.indexOf("Cerrar sesión"),
                "El nombre de la cuenta debe quedar junto al control de cerrar sesión");
    }

    private MockHttpSession login(String email) throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", email)
                        .param("password", PASSWORD))
                .andExpect(status().isFound())
                .andReturn().getRequest().getSession(false);
    }

    private Account createAccount(String name, String email, Role role) {
        Account account = new Account();
        account.setName(name);
        account.setLastName("Test");
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(PASSWORD));
        account.setRole(role);
        account.setCreatedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }

    private Long createService(String title, ServiceStatus status, Account expert, Category category, LocalDateTime createdAt) {
        OfferedService service = new OfferedService();
        service.setTitle(title);
        service.setDescription("Descripción de " + title);
        service.setBasePrice(new BigDecimal("1200.00"));
        service.setDeliveryTimeDays(10);
        service.setExpert(expert);
        service.setCategory(category);
        service.setAdmin(status == ServiceStatus.PENDING ? null : expert);
        service.setStatus(status);
        service.setCreatedAt(createdAt);
        service.setUpdatedAt(createdAt);
        return offeredServiceRepository.save(service).getId();
    }
}
