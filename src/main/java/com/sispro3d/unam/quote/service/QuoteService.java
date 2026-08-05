package com.sispro3d.unam.quote.service;

import com.sispro3d.unam.core.service.CrudService;
import com.sispro3d.unam.quote.domain.QuoteStatus;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.dto.QuoteResponse;

import java.util.List;

public interface QuoteService extends CrudService<QuoteRequest, QuoteResponse, Long> {
    QuoteResponse respond(Long id, Long expertId, QuoteRequest request);
    QuoteResponse accept(Long id, Long clientId);
    QuoteResponse reject(Long id, Long clientId);
    QuoteResponse expire(Long id);
    List<QuoteResponse> findByClientId(Long clientId);
    List<QuoteResponse> findByServiceId(Long offeredServiceId);
    List<QuoteResponse> findByExpertId(Long expertId);
    List<QuoteResponse> findByStatus(QuoteStatus status);
}
