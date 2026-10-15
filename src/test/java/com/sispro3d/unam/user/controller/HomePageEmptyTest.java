package com.sispro3d.unam.user.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:home_empty_test;DB_CLOSE_DELAY=-1;MODE=MySQL")
@ActiveProfiles("test")
@AutoConfigureMockMvc
class HomePageEmptyTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void landingLoadsWithEmptyStatesWithoutApprovedServicesOrCategories() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Todavía no hay servicios publicados")))
                .andExpect(content().string(containsString("Aún no hay categorías disponibles")));
    }
}
