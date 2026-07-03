package com.sispro3d.unam.quote.dto;

import com.sispro3d.unam.offeredservice.dto.OfferedServiceDTO;
import com.sispro3d.unam.user.dto.ClientDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuoteDTO {
    private int id;
    private String status; // PENDING, ACCEPTED, REJECTED, EXPIRED
    private BigDecimal totalAmount;
    private LocalDate validUntil;
    private String description;
    private LocalDateTime createdAt;
    private ClientDTO client;
    private OfferedServiceDTO offeredService;

    public QuoteDTO(int id) {
        this.id = id;
    }
}
