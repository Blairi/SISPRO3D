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

    @Override
    public String toString() {
        return "ServiceDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", basePrice=" + basePrice +
                ", adminId=" + (admin != null && admin.getAccount() != null ? admin.getAccount().getIdUser() : "null") +
                ", expertId=" + (expert != null && expert.getAccount() != null ? expert.getAccount().getIdUser() : "null") +
                ", category=" + category +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deliveryTimeDays=" + deliveryTimeDays +
                '}';
    }
}
