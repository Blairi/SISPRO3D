package com.sispro3d.unam.quote.mapper;

import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.dto.QuoteResponse;
import org.springframework.stereotype.Component;

@Component
public class QuoteMapper {

    /**
     * Maps the flat/scalar fields only. Relations (client, offeredService)
     * are NOT set here — the service layer resolves and assigns them,
     * since that requires repository access and business validation.
     */
    public Quote toEntity(QuoteRequest request) {
        if (request == null) {
            return null;
        }
        var quote = new Quote();
        quote.setDescription(request.getDescription());
        quote.setTotalAmount(request.getTotalAmount());
        quote.setValidUntil(request.getValidUntil());
        return quote;
    }

    public QuoteResponse toResponse(Quote quote) {
        if (quote == null) {
            return null;
        }
        return QuoteResponse.builder()
                .id(quote.getId())
                .status(quote.getStatus())
                .totalAmount(quote.getTotalAmount())
                .validUntil(quote.getValidUntil())
                .description(quote.getDescription())
                .clientId(quote.getClient() != null ? quote.getClient().getIdUser() : null)
                .offeredServiceId(quote.getOfferedService() != null ? quote.getOfferedService().getId() : null)
                .createdAt(quote.getCreatedAt())
                .build();
    }

    public void updateEntityFromRequest(QuoteRequest request, Quote quote) {
        quote.setDescription(request.getDescription());
        quote.setTotalAmount(request.getTotalAmount());
        quote.setValidUntil(request.getValidUntil());
    }
}
