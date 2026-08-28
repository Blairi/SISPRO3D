package com.sispro3d.unam.offeredservice.dto;

import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferedServiceResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal basePrice;
    private Long expertId;
    private String expertName;
    private String expertEmail;
    private Long adminId;
    private Long categoryId;
    private String categoryName;
    private ServiceStatus status;
    private Integer deliveryTimeDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
