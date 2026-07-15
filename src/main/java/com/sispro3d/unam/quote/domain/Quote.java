package com.sispro3d.unam.quote.domain;

import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.user.domain.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Quote {
    private int id;
    private String status;
    private BigDecimal totalAmount;
    private LocalDate validUntil;
    private String description;
    private LocalDateTime createdAt;
    private Account client;
    private OfferedService offeredService;

    public Quote(int id) {
        this.id = id;
    }
}
