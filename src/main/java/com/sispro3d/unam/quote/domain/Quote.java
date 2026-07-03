package com.sispro3d.unam.quote.domain;

import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.user.domain.Client;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quote {
    private int id;
    private String status; // PENDING, ACCEPTED, REJECTED, EXPIRED
    private BigDecimal totalAmount;
    private LocalDate validUntil;
    private String description;
    private LocalDateTime createdAt;
    private Client client;
    private OfferedService offeredService;

    public Quote(int id) {
        this.id = id;
    }
}
