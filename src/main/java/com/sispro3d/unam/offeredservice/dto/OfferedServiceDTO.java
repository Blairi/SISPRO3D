package com.sispro3d.unam.offeredservice.dto;

import com.sispro3d.unam.category.dto.CategoryDTO;
import com.sispro3d.unam.user.dto.AdminDTO;
import com.sispro3d.unam.user.dto.ExpertDTO;
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
    private AdminDTO admin; // null mientras no lo aprueba
    private ExpertDTO expert;
    private CategoryDTO category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int deliveryTimeDays;

    public OfferedServiceDTO(int id) {
        this.id = id;
    }
}
