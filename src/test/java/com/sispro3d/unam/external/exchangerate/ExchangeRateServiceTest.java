package com.sispro3d.unam.external.exchangerate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeRateServiceTest {

    private StubExchangeRateClient exchangeRateClient;

    private ExchangeRateProperties properties;

    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        exchangeRateClient = new StubExchangeRateClient();
        properties = new ExchangeRateProperties();
        exchangeRateService = new ExchangeRateService(properties, exchangeRateClient);
    }

    @Test
    void convertsAmountUsingTheFetchedRate() {
        exchangeRateClient.rate = new BigDecimal("0.058");

        assertThat(exchangeRateService.convert(new BigDecimal("1000.00")))
                .isEqualByComparingTo("58.00");
    }

    @Test
    void roundsToTwoDecimalsHalfUp() {
        exchangeRateClient.rate = new BigDecimal("0.05835");

        assertThat(exchangeRateService.convert(new BigDecimal("100.00")))
                .isEqualByComparingTo("5.84");
    }

    @Test
    void usesTheCachedRateAcrossLookupsWithinTheDay() {
        exchangeRateClient.rate = new BigDecimal("0.058");

        exchangeRateService.convert(new BigDecimal("100.00"));
        exchangeRateService.convert(new BigDecimal("200.00"));

        assertThat(exchangeRateClient.calls()).isEqualTo(1);
    }

    @Test
    void returnsNullWhenTheClientHasNoRate() {
        exchangeRateClient.rate = null;

        assertThat(exchangeRateService.convert(new BigDecimal("100.00"))).isNull();
    }

    @Test
    void returnsNullAndSkipsTheClientWhenDisabled() {
        properties.setEnabled(false);
        exchangeRateClient.rate = new BigDecimal("0.058");

        assertThat(exchangeRateService.convert(new BigDecimal("100.00"))).isNull();
        assertThat(exchangeRateClient.calls()).isZero();
    }

    static class StubExchangeRateClient implements ExchangeRateClient {

        private final AtomicInteger calls = new AtomicInteger();

        volatile BigDecimal rate;

        int calls() {
            return calls.get();
        }

        @Override
        public Optional<BigDecimal> fetchRate(String base, String target) {
            calls.incrementAndGet();
            return Optional.ofNullable(rate);
        }
    }
}
