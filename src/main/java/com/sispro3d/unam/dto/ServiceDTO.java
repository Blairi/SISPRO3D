package com.sispro3d.unam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {
    private int id;
    private String title;
    private String description;
    private BigDecimal basePrice;
    private AdminDTO admin; // null mientras no lo aprueba
    private ExpertDTO expert;
    private CategoryDTO category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int deliveryTimeDays;

    public ServiceDTO(int id) {
        this.id = id;
    }
}
