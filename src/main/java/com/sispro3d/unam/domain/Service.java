package com.sispro3d.unam.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Service {
    private int id;
    private String title;
    private String description;
    private BigDecimal basePrice;
    private Admin admin; // null mientras no lo aprueba
    private Expert expert;
    private Category category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int deliveryTimeDays;

    public Service(int id) {
        this.id = id;
    }
}