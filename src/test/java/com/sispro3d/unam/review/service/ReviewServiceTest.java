package com.sispro3d.unam.review.service;

import com.sispro3d.unam.category.dto.CategoryRequest;
import com.sispro3d.unam.category.dto.CategoryResponse;
import com.sispro3d.unam.category.service.CategoryService;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceRequest;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.review.dto.ReviewRequest;
import com.sispro3d.unam.review.dto.ReviewResponse;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.dto.AccountRequest;
import com.sispro3d.unam.user.dto.AccountResponse;
import com.sispro3d.unam.user.service.AccountService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OfferedServiceService offeredServiceService;

    @Autowired
    private AccountService accountService;

    private int clientId;
    private int offeredServiceId;
    private ReviewResponse created;

    @BeforeEach
    void setUp() {
        CategoryResponse category = categoryService.create(CategoryRequest.builder()
                .name("Test-Cat-Rev")
                .description("Test-Desc-Rev")
                .build());

        AccountResponse expert = accountService.create(AccountRequest.builder()
                .name("Test-Exp-Rev")
                .lastName("Test-Ape-Rev")
                .email("test-exp-rev@unam.mx")
                .phone("+52 66666666")
                .password("test-pass")
                .role(Role.EXPERT)
                .specialty("Test-Esp-Rev")
                .build());

        OfferedServiceResponse service = offeredServiceService.create(OfferedServiceRequest.builder()
                .title("Test-Serv-Rev")
                .description("Test-Desc-Rev")
                .basePrice(new BigDecimal("200.00"))
                .expertId(expert.getIdUser())
                .categoryId(category.getId())
                .deliveryTimeDays(3)
                .build());
        offeredServiceId = service.getId();

        AccountResponse client = accountService.create(AccountRequest.builder()
                .name("Test-Cli-Rev")
                .lastName("Test-Ape-Cli-Rev")
                .email("test-cli-rev@unam.mx")
                .phone("+52 77777777")
                .password("test-pass")
                .role(Role.CLIENT)
                .build());
        clientId = client.getIdUser();

        created = reviewService.create(ReviewRequest.builder()
                .rating(5)
                .comment("Test-Review-Comment")
                .clientId(clientId)
                .offeredServiceId(offeredServiceId)
                .build());
    }

    @AfterEach
    void tearDown() {
        try { reviewService.delete((long) created.getId()); } catch (Exception ignored) {}
        try { accountService.delete((long) clientId); } catch (Exception ignored) {}
        try { offeredServiceService.delete((long) offeredServiceId); } catch (Exception ignored) {}
    }

    @Test
    void create_shouldPersistAndReturnResponse() {
        assertThat(created.getId()).isPositive();
        assertThat(created.getRating()).isEqualTo(5);
        assertThat(created.getComment()).isEqualTo("Test-Review-Comment");
    }

    @Test
    void findById_shouldReturnCreatedReview() {
        var found = reviewService.findById((long) created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getRating()).isEqualTo(5);
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        var result = reviewService.findById(99999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldContainCreatedReview() {
        List<ReviewResponse> all = reviewService.findAll();

        assertThat(all)
                .filteredOn(r -> r.getId() == created.getId())
                .singleElement()
                .matches(r -> r.getRating() == 5);
    }

    @Test
    void update_shouldModifyReview() {
        var updated = reviewService.update((long) created.getId(),
                ReviewRequest.builder()
                        .rating(3)
                        .comment("Test-Updated")
                        .clientId(clientId)
                        .offeredServiceId(offeredServiceId)
                        .build());

        assertThat(updated.getRating()).isEqualTo(3);
        assertThat(updated.getComment()).isEqualTo("Test-Updated");

        var reloaded = reviewService.findById((long) created.getId());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getRating()).isEqualTo(3);
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> reviewService.update(99999L,
                ReviewRequest.builder().rating(1).build()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrada");
    }

    @Test
    void delete_shouldRemoveReview() {
        reviewService.delete((long) created.getId());

        assertThat(reviewService.findById((long) created.getId())).isEmpty();
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> reviewService.delete(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrada");
    }

    @Test
    void existsById_shouldReturnTrueForExisting() {
        assertThat(reviewService.existsById((long) created.getId())).isTrue();
    }

    @Test
    void existsById_shouldReturnFalseForNonExisting() {
        assertThat(reviewService.existsById(99999L)).isFalse();
    }
}
