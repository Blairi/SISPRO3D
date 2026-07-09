package com.sispro3d.unam.offeredservice.dto;

import com.sispro3d.unam.core.dto.AdminRef;
import com.sispro3d.unam.core.dto.CategoryRef;
import com.sispro3d.unam.core.dto.ExpertRef;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferedServiceDTO {
    private int id;
    private String title;
    private String description;
    private BigDecimal basePrice;
    private AdminRef admin; // null mientras no lo aprueba
    private ExpertRef expert;
    private CategoryRef category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int deliveryTimeDays;

    public OfferedServiceDTO(int id) {
        this.id = id;
    }
}
