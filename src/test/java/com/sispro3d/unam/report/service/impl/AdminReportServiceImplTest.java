package com.sispro3d.unam.report.service.impl;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.quote.domain.QuoteStatus;
import com.sispro3d.unam.quote.repository.QuoteRepository;
import com.sispro3d.unam.report.dto.ActivitySummary;
import com.sispro3d.unam.report.dto.ExpertPerformance;
import com.sispro3d.unam.report.dto.MostRequestedService;
import com.sispro3d.unam.report.dto.ReportDateRange;
import com.sispro3d.unam.report.service.AdminReportService;
import com.sispro3d.unam.review.domain.Review;
import com.sispro3d.unam.review.repository.ReviewRepository;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminReportServiceImplTest {

    @Autowired
    private AdminReportService adminReportService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OfferedServiceRepository offeredServiceRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private final LocalDateTime base = LocalDateTime.of(2026, 2, 10, 12, 0);

    @Test
    void mostRequestedServices_ranksByQuoteCountAndOmitsUnquoted() {
        Account expert = account("Elena", "expert", Role.EXPERT);
        Account client = account("Carla", "client", Role.CLIENT);
        Category category = category("Modelado");
        OfferedService mostQuoted = service("MostQuoted", expert, category, ServiceStatus.APPROVED);
        OfferedService someQuoted = service("SomeQuoted", expert, category, ServiceStatus.APPROVED);
        service("NeverQuoted", expert, category, ServiceStatus.APPROVED);

        quote(client, mostQuoted, QuoteStatus.PENDING, null);
        quote(client, mostQuoted, QuoteStatus.PENDING, null);
        quote(client, mostQuoted, QuoteStatus.PENDING, null);
        quote(client, someQuoted, QuoteStatus.PENDING, null);

        List<MostRequestedService> ranking = adminReportService.mostRequestedServices(ReportDateRange.all());

        assertThat(ranking)
                .extracting(MostRequestedService::title, MostRequestedService::quoteCount)
                .containsExactly(
                        tuple("MostQuoted", 3L),
                        tuple("SomeQuoted", 1L));
    }

    @Test
    void mostRequestedServices_breaksTiesByTitleAscending() {
        Account expert = account("Elena", "expert", Role.EXPERT);
        Account client = account("Carla", "client", Role.CLIENT);
        Category category = category("Render");
        OfferedService beta = service("Beta", expert, category, ServiceStatus.APPROVED);
        OfferedService alpha = service("Alfa", expert, category, ServiceStatus.APPROVED);

        quote(client, beta, QuoteStatus.PENDING, null);
        quote(client, alpha, QuoteStatus.PENDING, null);

        List<MostRequestedService> ranking = adminReportService.mostRequestedServices(ReportDateRange.all());

        assertThat(ranking)
                .extracting(MostRequestedService::title)
                .containsExactly("Alfa", "Beta");
    }

    @Test
    void mostRequestedServices_countsResultingOrders() {
        Account expert = account("Elena", "expert", Role.EXPERT);
        Account client = account("Carla", "client", Role.CLIENT);
        Category category = category("Animación");
        OfferedService svc = service("Animated", expert, category, ServiceStatus.APPROVED);
        Quote answered = quote(client, svc, QuoteStatus.ACCEPTED, BigDecimal.valueOf(100));
        quote(client, svc, QuoteStatus.PENDING, null);
        order(answered, WorkOrderStatus.IN_PROGRESS);

        List<MostRequestedService> ranking = adminReportService.mostRequestedServices(ReportDateRange.all());

        assertThat(ranking).singleElement().satisfies(row -> {
            assertThat(row.quoteCount()).isEqualTo(2L);
            assertThat(row.orderCount()).isEqualTo(1L);
            assertThat(row.categoryName()).isEqualTo("Animación");
            assertThat(row.expertName()).isEqualTo("Elena Test");
        });
    }

    @Test
    void mostRequestedServices_respectsDateRange() {
        Account expert = account("Elena", "expert", Role.EXPERT);
        Account client = account("Carla", "client", Role.CLIENT);
        Category category = category("Modelado");
        OfferedService svc = service("RangeSvc", expert, category, ServiceStatus.APPROVED);
        quoteAt(client, svc, QuoteStatus.PENDING, LocalDateTime.of(2026, 2, 15, 10, 0));
        quoteAt(client, svc, QuoteStatus.PENDING, LocalDateTime.of(2026, 3, 15, 10, 0));

        ReportDateRange february = ReportDateRange.of(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));

        assertThat(adminReportService.mostRequestedServices(february))
                .singleElement()
                .extracting(MostRequestedService::quoteCount)
                .isEqualTo(1L);
        assertThat(adminReportService.mostRequestedServices(ReportDateRange.all()).get(0).quoteCount())
                .isEqualTo(2L);
    }

    @Test
    void activitySummary_countsByStatusIncludingZeros() {
        Account admin = account("Ana", "admin", Role.ADMIN);
        Account expert = account("Elena", "expert", Role.EXPERT);
        Account client = account("Carla", "client", Role.CLIENT);
        Category category = category("Modelado");
        OfferedService pending = service("Pending", expert, category, ServiceStatus.PENDING);
        service("Approved", expert, category, ServiceStatus.APPROVED);
        Quote quote = quote(client, pending, QuoteStatus.PENDING, null);
        order(quote, WorkOrderStatus.IN_PROGRESS);
        review(client, pending, 4);

        ActivitySummary summary = adminReportService.activitySummary(ReportDateRange.all());

        assertThat(summary.accountsByRole()).containsOnly(
                entry("ADMIN", 1L), entry("CLIENT", 1L), entry("EXPERT", 1L));
        assertThat(summary.servicesByStatus()).containsOnly(
                entry("PENDING", 1L), entry("APPROVED", 1L), entry("REJECTED", 0L));
        assertThat(summary.quotesByStatus()).containsOnly(
                entry("PENDING", 1L), entry("ACCEPTED", 0L), entry("REJECTED", 0L), entry("EXPIRED", 0L));
        assertThat(summary.ordersByStatus()).containsOnly(
                entry("PENDING", 0L), entry("IN_PROGRESS", 1L), entry("IN_REVIEW", 0L),
                entry("COMPLETED", 0L), entry("CANCELED", 0L));
        assertThat(summary.reviewCount()).isEqualTo(1L);
    }

    @Test
    void expertPerformance_reportsMetricsForExpertWithReviews() {
        Account expert = account("Elena", "expert", Role.EXPERT);
        Account clientA = account("Carla", "clientA", Role.CLIENT);
        Account clientB = account("Bruno", "clientB", Role.CLIENT);
        Category category = category("Modelado");
        OfferedService svc = service("Performed", expert, category, ServiceStatus.APPROVED);

        quote(clientA, svc, QuoteStatus.PENDING, null);
        Quote answered = quote(clientA, svc, QuoteStatus.ACCEPTED, BigDecimal.valueOf(250));
        order(answered, WorkOrderStatus.COMPLETED);
        order(quote(clientB, svc, QuoteStatus.ACCEPTED, BigDecimal.valueOf(90)), WorkOrderStatus.IN_PROGRESS);
        review(clientA, svc, 4);
        review(clientB, svc, 5);

        List<ExpertPerformance> rows = adminReportService.expertPerformance(ReportDateRange.all());

        assertThat(rows).singleElement().satisfies(row -> {
            assertThat(row.expertName()).isEqualTo("Elena Test");
            assertThat(row.serviceCount()).isEqualTo(1L);
            assertThat(row.answeredQuotes()).isEqualTo(2L);
            assertThat(row.completedOrders()).isEqualTo(1L);
            assertThat(row.ordersByStatus()).containsEntry("COMPLETED", 1L)
                    .containsEntry("IN_PROGRESS", 1L)
                    .containsEntry("PENDING", 0L)
                    .containsEntry("IN_REVIEW", 0L)
                    .containsEntry("CANCELED", 0L);
            assertThat(row.averageRating()).isEqualTo(4.5);
            assertThat(row.reviewCount()).isEqualTo(2L);
        });
    }

    @Test
    void expertPerformance_marksExpertWithoutReviewsAndOmitsAccountsWithoutServices() {
        Account expert = account("Elena", "expert", Role.EXPERT);
        account("Sofia", "expertNoServices", Role.EXPERT);
        Account client = account("Carla", "client", Role.CLIENT);
        Category category = category("Modelado");
        OfferedService svc = service("NoReviews", expert, category, ServiceStatus.APPROVED);
        quote(client, svc, QuoteStatus.PENDING, null);

        List<ExpertPerformance> rows = adminReportService.expertPerformance(ReportDateRange.all());

        assertThat(rows).singleElement().satisfies(row -> {
            assertThat(row.expertName()).isEqualTo("Elena Test");
            assertThat(row.averageRating()).isNull();
            assertThat(row.reviewCount()).isZero();
        });
    }

    private Account account(String name, String emailLocal, Role role) {
        Account account = new Account();
        account.setName(name);
        account.setLastName("Test");
        account.setEmail(emailLocal + "@reports.test");
        account.setPassword("$2a$10$6OFUT4kW8.Ix/obINbfLiO3bvcF.4BN4.AVcxQBIIQvYDwXhi0azm");
        account.setRole(role);
        account.setCreatedAt(base);
        return accountRepository.save(account);
    }

    private Category category(String name) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(name);
        return categoryRepository.save(category);
    }

    private OfferedService service(String title, Account expert, Category category, ServiceStatus status) {
        OfferedService service = new OfferedService();
        service.setTitle(title);
        service.setDescription(title);
        service.setBasePrice(BigDecimal.valueOf(100));
        service.setExpert(expert);
        service.setCategory(category);
        service.setStatus(status);
        service.setDeliveryTimeDays(3);
        service.setCreatedAt(base);
        return offeredServiceRepository.save(service);
    }

    private Quote quote(Account client, OfferedService service, QuoteStatus status, BigDecimal amount) {
        return quoteAt(client, service, status, base, amount);
    }

    private Quote quoteAt(Account client, OfferedService service, QuoteStatus status, LocalDateTime createdAt) {
        return quoteAt(client, service, status, createdAt, null);
    }

    private Quote quoteAt(Account client, OfferedService service, QuoteStatus status, LocalDateTime createdAt, BigDecimal amount) {
        Quote quote = new Quote();
        quote.setClient(client);
        quote.setOfferedService(service);
        quote.setStatus(status);
        quote.setTotalAmount(amount);
        quote.setCreatedAt(createdAt);
        return quoteRepository.save(quote);
    }

    private WorkOrder order(Quote quote, WorkOrderStatus status) {
        WorkOrder order = new WorkOrder();
        order.setQuote(quote);
        order.setStatus(status);
        order.setCreatedAt(base);
        return workOrderRepository.save(order);
    }

    private Review review(Account client, OfferedService service, int rating) {
        Review review = new Review();
        review.setClient(client);
        review.setOfferedService(service);
        review.setRating(rating);
        review.setCreatedAt(base);
        return reviewRepository.save(review);
    }
}
