package com.sispro3d.unam.offeredservice.service.impl;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.dto.ServiceSearchCriteria;
import com.sispro3d.unam.offeredservice.dto.ServiceSortOrder;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OfferedServiceSearchTest {

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
        account.setName("Elena");
        account.setLastName("Vargas");
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

    private Long createService(String title, String description, Account expert, Category category,
                               String price, int deliveryDays, ServiceStatus status, LocalDateTime createdAt) {
        var service = new OfferedService();
        service.setTitle(title);
        service.setDescription(description);
        service.setBasePrice(new BigDecimal(price));
        service.setExpert(expert);
        service.setCategory(category);
        service.setStatus(status);
        service.setDeliveryTimeDays(deliveryDays);
        service.setCreatedAt(createdAt);
        service.setUpdatedAt(createdAt);
        return offeredServiceRepository.save(service).getId();
    }

    private List<OfferedServiceResponse> search(String query, Long categoryId, String minPrice, String maxPrice,
                                                Integer maxDeliveryDays, ServiceSortOrder sort) {
        return offeredServiceService.search(new ServiceSearchCriteria(
                query, categoryId,
                minPrice == null ? null : new BigDecimal(minPrice),
                maxPrice == null ? null : new BigDecimal(maxPrice),
                maxDeliveryDays, sort));
    }

    private List<String> titles(List<OfferedServiceResponse> services) {
        return services.stream().map(OfferedServiceResponse::getTitle).toList();
    }

    @Test
    void search_matchesTitleDescriptionAndExpertCaseInsensitively() {
        var expert = createExpert("search.expert@sispro3d.com");
        var category = createCategory("Modelado");
        var now = LocalDateTime.of(2026, 2, 10, 12, 0);
        createService("Dragón épico", "Escultura digital", expert, category, "100.00", 5, ServiceStatus.APPROVED, now);
        createService("Nave espacial", "Modelo sci-fi", expert, category, "200.00", 6, ServiceStatus.APPROVED, now);

        assertThat(titles(search("DRAGÓN", null, null, null, null, null))).containsExactly("Dragón épico");
        assertThat(titles(search("escultura", null, null, null, null, null))).containsExactly("Dragón épico");
        assertThat(titles(search("elena", null, null, null, null, null)))
                .containsExactlyInAnyOrder("Dragón épico", "Nave espacial");
    }

    @Test
    void search_blankQueryReturnsAllApproved() {
        var expert = createExpert("search.blank@sispro3d.com");
        var category = createCategory("Modelado");
        var now = LocalDateTime.of(2026, 2, 10, 12, 0);
        createService("Uno", "d", expert, category, "100.00", 5, ServiceStatus.APPROVED, now);
        createService("Dos", "d", expert, category, "200.00", 6, ServiceStatus.APPROVED, now);

        assertThat(search("", null, null, null, null, null)).hasSize(2);
        assertThat(search("   ", null, null, null, null, null)).hasSize(2);
        assertThat(search(null, null, null, null, null, null)).hasSize(2);
    }

    @Test
    void search_filtersByCategory() {
        var expert = createExpert("search.cat@sispro3d.com");
        var modelado = createCategory("Modelado");
        var animacion = createCategory("Animación");
        var now = LocalDateTime.of(2026, 2, 10, 12, 0);
        createService("En modelado", "d", expert, modelado, "100.00", 5, ServiceStatus.APPROVED, now);
        createService("En animación", "d", expert, animacion, "200.00", 6, ServiceStatus.APPROVED, now);

        assertThat(titles(search(null, modelado.getId(), null, null, null, null))).containsExactly("En modelado");
    }

    @Test
    void search_filtersByPriceRangeInclusiveAndByEachBound() {
        var expert = createExpert("search.price@sispro3d.com");
        var category = createCategory("Modelado");
        var now = LocalDateTime.of(2026, 2, 10, 12, 0);
        createService("Barato", "d", expert, category, "100.00", 5, ServiceStatus.APPROVED, now);
        createService("Medio", "d", expert, category, "200.00", 5, ServiceStatus.APPROVED, now);
        createService("Caro", "d", expert, category, "300.00", 5, ServiceStatus.APPROVED, now);

        assertThat(titles(search(null, null, "100.00", "200.00", null, null)))
                .containsExactlyInAnyOrder("Barato", "Medio");
        assertThat(titles(search(null, null, "200.00", null, null, null)))
                .containsExactlyInAnyOrder("Medio", "Caro");
        assertThat(titles(search(null, null, null, "200.00", null, null)))
                .containsExactlyInAnyOrder("Barato", "Medio");
    }

    @Test
    void search_filtersByMaxDeliveryDays() {
        var expert = createExpert("search.delivery@sispro3d.com");
        var category = createCategory("Modelado");
        var now = LocalDateTime.of(2026, 2, 10, 12, 0);
        createService("Rápido", "d", expert, category, "100.00", 3, ServiceStatus.APPROVED, now);
        createService("Lento", "d", expert, category, "100.00", 30, ServiceStatus.APPROVED, now);

        assertThat(titles(search(null, null, null, null, 10, null))).containsExactly("Rápido");
    }

    @Test
    void search_combinesFiltersWithAnd() {
        var expert = createExpert("search.combo@sispro3d.com");
        var category = createCategory("Modelado");
        var other = createCategory("Animación");
        var now = LocalDateTime.of(2026, 2, 10, 12, 0);
        createService("Dragón rápido", "d", expert, category, "150.00", 4, ServiceStatus.APPROVED, now);
        createService("Dragón lento", "d", expert, category, "150.00", 40, ServiceStatus.APPROVED, now);
        createService("Dragón caro", "d", expert, category, "900.00", 4, ServiceStatus.APPROVED, now);
        createService("Dragón otra cat", "d", expert, other, "150.00", 4, ServiceStatus.APPROVED, now);

        assertThat(titles(search("dragón", category.getId(), "100.00", "200.00", 10, null)))
                .containsExactly("Dragón rápido");
    }

    @Test
    void search_orderNewestByDefaultAndNullsLast() {
        var expert = createExpert("search.order@sispro3d.com");
        var category = createCategory("Modelado");
        createService("Sin fecha", "d", expert, category, "100.00", 5, ServiceStatus.APPROVED, null);
        createService("Viejo", "d", expert, category, "100.00", 5, ServiceStatus.APPROVED,
                LocalDateTime.of(2026, 1, 1, 10, 0));
        createService("Nuevo", "d", expert, category, "100.00", 5, ServiceStatus.APPROVED,
                LocalDateTime.of(2026, 3, 1, 10, 0));

        assertThat(titles(search(null, null, null, null, null, null)))
                .containsExactly("Nuevo", "Viejo", "Sin fecha");
        assertThat(titles(search(null, null, null, null, null, ServiceSortOrder.NEWEST)))
                .containsExactly("Nuevo", "Viejo", "Sin fecha");
    }

    @Test
    void search_orderByPriceAscendingAndDescending() {
        var expert = createExpert("search.orderprice@sispro3d.com");
        var category = createCategory("Modelado");
        var now = LocalDateTime.of(2026, 2, 10, 12, 0);
        createService("Medio", "d", expert, category, "200.00", 5, ServiceStatus.APPROVED, now);
        createService("Caro", "d", expert, category, "300.00", 5, ServiceStatus.APPROVED, now);
        createService("Barato", "d", expert, category, "100.00", 5, ServiceStatus.APPROVED, now);

        assertThat(titles(search(null, null, null, null, null, ServiceSortOrder.PRICE_ASC)))
                .containsExactly("Barato", "Medio", "Caro");
        assertThat(titles(search(null, null, null, null, null, ServiceSortOrder.PRICE_DESC)))
                .containsExactly("Caro", "Medio", "Barato");
    }

    @Test
    void search_onlyReturnsApprovedServices() {
        var expert = createExpert("search.approved@sispro3d.com");
        var category = createCategory("Modelado");
        var now = LocalDateTime.of(2026, 2, 10, 12, 0);
        createService("Pendiente coincidente", "d", expert, category, "100.00", 5, ServiceStatus.PENDING, now);
        createService("Rechazado coincidente", "d", expert, category, "100.00", 5, ServiceStatus.REJECTED, now);
        createService("Aprobado coincidente", "d", expert, category, "100.00", 5, ServiceStatus.APPROVED, now);

        assertThat(titles(search("coincidente", null, null, null, null, null)))
                .containsExactly("Aprobado coincidente");
    }

    @Test
    void search_zeroMatchesReturnsEmpty() {
        var expert = createExpert("search.empty@sispro3d.com");
        var category = createCategory("Modelado");
        var now = LocalDateTime.of(2026, 2, 10, 12, 0);
        createService("Servicio", "d", expert, category, "100.00", 5, ServiceStatus.APPROVED, now);

        assertThat(search("inexistente", null, null, null, null, null)).isEmpty();
    }
}
