package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.external.exchangerate.ExchangeRateClient;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CurrencyConversionWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StubExchangeRateClient exchangeRateClient;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OfferedServiceRepository offeredServiceRepository;

    private Long serviceId;

    @TestConfiguration
    static class StubConfig {

        @Bean
        @Primary
        StubExchangeRateClient stubExchangeRateClient() {
            return new StubExchangeRateClient();
        }
    }

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setName("Modelado");
        category.setDescription("Servicios de modelado 3D");
        categoryRepository.save(category);

        Account expert = new Account();
        expert.setName("Lucia");
        expert.setLastName("Paredes");
        expert.setEmail("lucia.currency@test.mx");
        expert.setPassword("$2a$10$6OFUT4kW8.Ix/obINbfLiO3bvcF.4BN4.AVcxQBIIQvYDwXhi0azm");
        expert.setRole(Role.EXPERT);
        expert.setCreatedAt(LocalDateTime.now());
        Long expertId = accountRepository.save(expert).getIdUser();

        Account admin = new Account();
        admin.setName("Ricardo");
        admin.setLastName("Solano");
        admin.setEmail("admin.currency@test.mx");
        admin.setPassword("$2a$10$3KduQCbjkdDMClf3EHZ3rOUwRD2sytH6h1Ayd.NrEYU1IfyJYpIim");
        admin.setRole(Role.ADMIN);
        admin.setCreatedAt(LocalDateTime.now());
        Long adminId = accountRepository.save(admin).getIdUser();

        OfferedService service = new OfferedService();
        service.setTitle("Modelado aprobado");
        service.setDescription("Descripción de modelado");
        service.setBasePrice(new BigDecimal("1000.00"));
        service.setDeliveryTimeDays(10);
        service.setExpert(accountRepository.getReferenceById(expertId));
        service.setCategory(category);
        service.setAdmin(accountRepository.getReferenceById(adminId));
        service.setStatus(ServiceStatus.APPROVED);
        service.setCreatedAt(LocalDateTime.now());
        service.setUpdatedAt(LocalDateTime.now());
        serviceId = offeredServiceRepository.save(service).getId();
    }

    @Test
    @Order(1)
    void listingOmitsUsdPriceWhenTheClientFails() throws Exception {
        exchangeRateClient.rate = null;

        mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("$1000.00")))
                .andExpect(content().string(not(containsString("USD"))));
    }

    @Test
    @Order(2)
    void listingShowsUsdPriceAlongsideTheMxnPrice() throws Exception {
        exchangeRateClient.rate = new BigDecimal("0.058");

        mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("$1000.00")))
                .andExpect(content().string(containsString("≈ $58.00 USD")));
    }

    @Test
    @Order(3)
    void detailShowsUsdPriceAlongsideTheMxnPrice() throws Exception {
        exchangeRateClient.rate = new BigDecimal("0.058");

        mockMvc.perform(get("/services/" + serviceId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("$1000.00")))
                .andExpect(content().string(containsString("≈ $58.00 USD")));
    }

    static class StubExchangeRateClient implements ExchangeRateClient {

        volatile BigDecimal rate = new BigDecimal("0.058");

        @Override
        public Optional<BigDecimal> fetchRate(String base, String target) {
            return Optional.ofNullable(rate);
        }
    }
}
