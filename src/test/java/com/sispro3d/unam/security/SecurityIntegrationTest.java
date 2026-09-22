package com.sispro3d.unam.security;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
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

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long adminId;
    private Long expertId;
    private Long clientId;

    @BeforeEach
    void setUp() {
        adminId = createAccount("admin@sec.test", Role.ADMIN);
        expertId = createAccount("expert@sec.test", Role.EXPERT);
        clientId = createAccount("client@sec.test", Role.CLIENT);
    }

    private Long createAccount(String email, Role role) {
        Account account = new Account();
        account.setName("Sec");
        account.setLastName("Test");
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode("test123"));
        account.setRole(role);
        account.setCreatedAt(LocalDateTime.now());
        return accountRepository.save(account).getIdUser();
    }

    private MockHttpSession login(String email, String password) throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().isFound())
                .andReturn().getRequest().getSession(false);
    }

    @Test
    void anonymousGetAdminDashboard_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void anonymousGetExpertDashboard_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/expert/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void loginAsClient_isDeniedOnAdminDashboard() throws Exception {
        MockHttpSession session = login("client@sec.test", "test123");
        mockMvc.perform(get("/admin/dashboard").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void loginAsAdmin_reachesAdminDashboard_withSessionUser() throws Exception {
        MockHttpSession session = login("admin@sec.test", "test123");
        mockMvc.perform(get("/admin/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("sessionUser"));
    }

    @Test
    void loginAsExpert_reachesExpertDashboard() throws Exception {
        MockHttpSession session = login("expert@sec.test", "test123");
        mockMvc.perform(get("/expert/dashboard").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousPublicPages_andStaticAssets_areServed() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/register")).andExpect(status().isOk());
        mockMvc.perform(get("/vendor/tailwind/tailwind.min.js")).andExpect(status().isOk());
        mockMvc.perform(get("/css/sispro3d.css")).andExpect(status().isOk());
    }

    @Test
    void anonymousHome_hasNoSessionUser() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("sessionUser"));
    }

    @Test
    void loginWithWrongPassword_redirectsWithError() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", "admin@sec.test")
                        .param("password", "incorrecta"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void logout_terminatesSession() throws Exception {
        MockHttpSession session = login("admin@sec.test", "test123");
        mockMvc.perform(post("/logout").with(csrf()).session(session))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    void restApi_postWithoutAuthAndWithoutCsrf_stillWorks() throws Exception {
        Category category = new Category();
        category.setName("Modelado");
        category.setDescription("Servicios 3D");
        Long categoryId = categoryRepository.save(category).getId();

        String body = """
                {
                  "title": "Modelado de prueba",
                  "description": "Descripcion",
                  "basePrice": 1500.00,
                  "expertId": %d,
                  "categoryId": %d,
                  "deliveryTimeDays": 7
                }
                """.formatted(expertId, categoryId);

        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }
}