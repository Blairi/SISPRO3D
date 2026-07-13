package com.sispro3d.unam.quote.service;

import com.sispro3d.unam.core.service.CrudService;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.dto.QuoteResponse;

public interface QuoteService extends CrudService<QuoteRequest, QuoteResponse, Long> {
}
