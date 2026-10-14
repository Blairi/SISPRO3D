package com.sispro3d.unam.core.storage;

import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class StorageWebConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        Account account = new Account();
        account.setName("Storage");
        account.setLastName("Tester");
        account.setEmail("storage@sec.test");
        account.setPassword(passwordEncoder.encode("test123"));
        account.setRole(Role.CLIENT);
        account.setCreatedAt(LocalDateTime.now());
        accountRepository.save(account);
    }

    private MockHttpSession login() throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", "storage@sec.test")
                        .param("password", "test123"))
                .andExpect(status().isFound())
                .andReturn().getRequest().getSession(false);
    }

    @Test
    void storedFile_isServedAtFilesPath_whenAuthenticated() throws Exception {
        String reference = fileStorageService.store(
                new MockMultipartFile("file", "render.png", "image/png", "png-bytes".getBytes()),
                "previews");

        mockMvc.perform(get("/files/" + reference).session(login()))
                .andExpect(status().isOk())
                .andExpect(content().bytes("png-bytes".getBytes()));
    }

    @Test
    void storedFile_requiresAuthentication() throws Exception {
        String reference = fileStorageService.store(
                new MockMultipartFile("file", "render.png", "image/png", "png-bytes".getBytes()),
                "previews");

        mockMvc.perform(get("/files/" + reference))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
