package com.sispro3d.unam.offeredservice.domain;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.user.domain.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OfferedService {
    private int id;
    private String title;
    private String description;
    private BigDecimal basePrice;
    private Account admin;
    private Account expert;
    private Category category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int deliveryTimeDays;

    public OfferedService(int id) {
        this.id = id;
    }
}
