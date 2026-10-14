package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.quote.domain.QuoteStatus;
import com.sispro3d.unam.quote.repository.QuoteRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AdminReportsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OfferedServiceRepository offeredServiceRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        createAccount("admin@reports.test", Role.ADMIN);
        createAccount("expert@reports.test", Role.EXPERT);
        createAccount("client@reports.test", Role.CLIENT);
    }

    @Test
    void anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void client_isDenied() throws Exception {
        MockHttpSession session = login("client@reports.test", "test123");
        mockMvc.perform(get("/admin/reports").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void expert_isDenied() throws Exception {
        MockHttpSession session = login("expert@reports.test", "test123");
        mockMvc.perform(get("/admin/reports").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void admin_seesReportsWithSeededData() throws Exception {
        Account expert = accountRepository.findByEmail("expert@reports.test").orElseThrow();
        Account client = accountRepository.findByEmail("client@reports.test").orElseThrow();
        Category category = category("Modelado");
        OfferedService service = service("Solicitado", expert, category);
        quote(client, service, QuoteStatus.PENDING, LocalDateTime.of(2026, 2, 15, 10, 0));
        quote(client, service, QuoteStatus.PENDING, LocalDateTime.of(2026, 2, 20, 10, 0));

        MockHttpSession session = login("admin@reports.test", "test123");

        mockMvc.perform(get("/admin/reports").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Servicios más solicitados")))
                .andExpect(content().string(containsString("Desempeño de expertos")))
                .andExpect(content().string(containsString("Resumen de actividad")))
                .andExpect(content().string(containsString("Servicios por estado")))
                .andExpect(content().string(containsString("Solicitado")));
    }

    @Test
    void admin_seesEmptyStatesWhenNoDemand() throws Exception {
        MockHttpSession session = login("admin@reports.test", "test123");

        mockMvc.perform(get("/admin/reports").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No hay servicios solicitados para el rango seleccionado.")))
                .andExpect(content().string(containsString("No hay expertos con servicios para el rango seleccionado.")));
    }

    @Test
    void navLink_isVisibleToAdminOnly() throws Exception {
        MockHttpSession admin = login("admin@reports.test", "test123");
        mockMvc.perform(get("/admin/reports").session(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/admin/reports\"")));

        MockHttpSession client = login("client@reports.test", "test123");
        mockMvc.perform(get("/client/quotes").session(client))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("/admin/reports"))));
    }

    @Test
    void validRange_isApplied() throws Exception {
        MockHttpSession session = login("admin@reports.test", "test123");

        mockMvc.perform(get("/admin/reports")
                        .param("from", "2026-02-01")
                        .param("to", "2026-02-28")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("from", LocalDate.of(2026, 2, 1)))
                .andExpect(model().attribute("to", LocalDate.of(2026, 2, 28)))
                .andExpect(model().attributeDoesNotExist("rangeError"));
    }

    @Test
    void fromOnly_isApplied() throws Exception {
        MockHttpSession session = login("admin@reports.test", "test123");

        mockMvc.perform(get("/admin/reports").param("from", "2026-02-01").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("from", LocalDate.of(2026, 2, 1)))
                .andExpect(model().attribute("to", nullValue()));
    }

    @Test
    void toOnly_isApplied() throws Exception {
        MockHttpSession session = login("admin@reports.test", "test123");

        mockMvc.perform(get("/admin/reports").param("to", "2026-02-28").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("to", LocalDate.of(2026, 2, 28)))
                .andExpect(model().attribute("from", nullValue()));
    }

    @Test
    void invertedRange_showsValidationMessage() throws Exception {
        MockHttpSession session = login("admin@reports.test", "test123");

        mockMvc.perform(get("/admin/reports")
                        .param("from", "2026-03-01")
                        .param("to", "2026-02-01")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("rangeError"))
                .andExpect(content().string(containsString("no puede ser posterior")));
    }

    private Account createAccount(String email, Role role) {
        Account account = new Account();
        account.setName("Reports");
        account.setLastName("Test");
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode("test123"));
        account.setRole(role);
        account.setCreatedAt(LocalDateTime.of(2026, 2, 10, 12, 0));
        return accountRepository.save(account);
    }

    private MockHttpSession login(String email, String password) throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().isFound())
                .andReturn().getRequest().getSession(false);
    }

    private Category category(String name) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(name);
        return categoryRepository.save(category);
    }

    private OfferedService service(String title, Account expert, Category category) {
        OfferedService service = new OfferedService();
        service.setTitle(title);
        service.setDescription(title);
        service.setBasePrice(BigDecimal.valueOf(100));
        service.setExpert(expert);
        service.setCategory(category);
        service.setStatus(ServiceStatus.APPROVED);
        service.setDeliveryTimeDays(3);
        service.setCreatedAt(LocalDateTime.of(2026, 2, 10, 12, 0));
        return offeredServiceRepository.save(service);
    }

    private Quote quote(Account client, OfferedService service, QuoteStatus status, LocalDateTime createdAt) {
        Quote quote = new Quote();
        quote.setClient(client);
        quote.setOfferedService(service);
        quote.setStatus(status);
        quote.setCreatedAt(createdAt);
        return quoteRepository.save(quote);
    }
}
