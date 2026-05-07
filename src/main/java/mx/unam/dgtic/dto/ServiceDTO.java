package mx.unam.dgtic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
