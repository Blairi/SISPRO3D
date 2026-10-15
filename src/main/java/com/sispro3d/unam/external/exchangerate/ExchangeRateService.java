package com.sispro3d.unam.external.exchangerate;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class ExchangeRateService {

    private final ExchangeRateProperties properties;

    private final ExchangeRateClient exchangeRateClient;

    public ExchangeRateService(ExchangeRateProperties properties, ExchangeRateClient exchangeRateClient) {
        this.properties = properties;
        this.exchangeRateClient = exchangeRateClient;
    }

    private volatile BigDecimal cachedRate;

    private volatile LocalDate cachedDate;

    public BigDecimal convert(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        BigDecimal rate = currentRate();
        if (rate == null) {
            return null;
        }
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal currentRate() {
        if (!properties.isEnabled()) {
            return null;
        }

        LocalDate today = LocalDate.now();
        BigDecimal cached = cachedRate;
        if (cached != null && today.equals(cachedDate)) {
            return cached;
        }

        BigDecimal fetched = exchangeRateClient
                .fetchRate(properties.getBaseCurrency(), properties.getTargetCurrency())
                .orElse(null);

        if (fetched != null) {
            cachedRate = fetched;
            cachedDate = today;
        }
        return fetched;
    }
}
