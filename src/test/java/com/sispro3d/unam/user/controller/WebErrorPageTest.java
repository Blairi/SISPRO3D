package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class WebErrorPageTest {

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

    private Long pendingServiceId;

    @BeforeEach
    void setUp() {
        Account expert = createAccount("Lucia", "error.expert@sispro3d.com", Role.EXPERT);
        createAccount("Cliente", "error.client@sispro3d.com", Role.CLIENT);

        Category category = new Category();
        category.setName("Modelado");
        category.setDescription("Servicios de modelado 3D");
        categoryRepository.save(category);

        OfferedService pending = new OfferedService();
        pending.setTitle("Servicio pendiente");
        pending.setDescription("Descripción pendiente");
        pending.setBasePrice(new BigDecimal("1200.00"));
        pending.setDeliveryTimeDays(10);
        pending.setExpert(expert);
        pending.setCategory(category);
        pending.setStatus(ServiceStatus.PENDING);
        pending.setCreatedAt(LocalDateTime.now());
        pending.setUpdatedAt(LocalDateTime.now());
        pendingServiceId = offeredServiceRepository.save(pending).getId();
    }

    @Test
    void unknownRoute_respondsNotFound() throws Exception {
        mockMvc.perform(get("/ruta-que-no-existe").session(login()))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingResource_respondsNotFound() throws Exception {
        mockMvc.perform(get("/services/" + pendingServiceId))
                .andExpect(status().isNotFound());
    }

    @Test
    void notFoundPage_isCustomAndSafe_forAnonymousVisitor() throws Exception {
        mockMvc.perform(errorPage(404))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Página no encontrada")))
                .andExpect(content().string(containsString("Volver al inicio")))
                .andExpect(content().string(containsString("Ver servicios")))
                .andExpect(content().string(containsString("Iniciar sesión")))
                .andExpect(content().string(not(containsString("not founded with id"))))
                .andExpect(content().string(not(containsString("Whitelabel"))));
    }

    @Test
    void notFoundPage_reflectsSession_forAuthenticatedVisitor() throws Exception {
        mockMvc.perform(errorPage(404).session(login()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Página no encontrada")))
                .andExpect(content().string(containsString("Mis cotizaciones")))
                .andExpect(content().string(not(containsString("Iniciar sesión"))));
    }

    @Test
    void internalErrorPage_isCustomAndSafe() throws Exception {
        mockMvc.perform(get("/error").accept(MediaType.TEXT_HTML))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Ocurrió un error inesperado")))
                .andExpect(content().string(containsString("Volver al inicio")))
                .andExpect(content().string(not(containsString("Exception"))))
                .andExpect(content().string(not(containsString("java."))));
    }

    @Test
    void genericFallbackPage_rendersForUnmappedStatus() throws Exception {
        mockMvc.perform(errorPage(418))
                .andExpect(status().isIAmATeapot())
                .andExpect(content().string(containsString("No pudimos completar tu solicitud")))
                .andExpect(content().string(containsString("Volver al inicio")));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder errorPage(int statusCode) {
        return get("/error")
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, statusCode)
                .accept(MediaType.TEXT_HTML);
    }

    private MockHttpSession login() throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", "error.client@sispro3d.com")
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
}
