package mx.unam.dgtic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteDTO {
    private int id;
    private String status; // PENDING, ACCEPTED, REJECTED, EXPIRED
    private BigDecimal totalAmount;
    private LocalDate validUntil;
    private String description;
    private LocalDateTime createdAt;
    private ClientDTO client;
    private ServiceDTO service;

    public QuoteDTO(int id) {
        this.id = id;
    }
}
