package com.sispro3d.unam.api.mapper;

import com.sispro3d.unam.api.dto.QuoteResponseDTO;
import com.sispro3d.unam.quote.domain.Quote;
import org.springframework.stereotype.Component;

@Component
public class ApiQuoteMapper {

    public QuoteResponseDTO toResponse(Quote entity) {
        if (entity == null) {
            return null;
        }
        return QuoteResponseDTO.builder()
                .id(entity.getId())
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .validUntil(entity.getValidUntil())
                .description(entity.getDescription())
                .clientId(entity.getClient() != null ? entity.getClient().getIdUser() : null)
                .offeredServiceId(entity.getOfferedService() != null ? entity.getOfferedService().getId() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}