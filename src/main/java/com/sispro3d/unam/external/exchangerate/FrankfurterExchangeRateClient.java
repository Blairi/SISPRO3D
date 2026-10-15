package com.sispro3d.unam.external.exchangerate;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Component
public class FrankfurterExchangeRateClient implements ExchangeRateClient {

    private final RestClient restClient;

    public FrankfurterExchangeRateClient(RestClient exchangeRateRestClient) {
        this.restClient = exchangeRateRestClient;
    }

    @Override
    public Optional<BigDecimal> fetchRate(String base, String target) {
        try {
            FrankfurterRatesResponse response = restClient.get()
                    .uri("/v1/latest?base={base}&symbols={target}", base, target)
                    .retrieve()
                    .body(FrankfurterRatesResponse.class);
            if (response == null || response.rates() == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(response.rates().get(target));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    record FrankfurterRatesResponse(String base, String date, Map<String, BigDecimal> rates) {
    }
}
