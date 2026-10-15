package com.sispro3d.unam.external.exchangerate;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FrankfurterExchangeRateClientTest {

    @Test
    void mapsTargetRateFromResponse() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.frankfurter.dev");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.frankfurter.dev/v1/latest?base=MXN&symbols=USD"))
                .andRespond(withSuccess(
                        "{\"base\":\"MXN\",\"date\":\"2026-10-14\",\"rates\":{\"USD\":0.058}}",
                        MediaType.APPLICATION_JSON));

        FrankfurterExchangeRateClient client = new FrankfurterExchangeRateClient(builder.build());

        assertThat(client.fetchRate("MXN", "USD")).contains(new BigDecimal("0.058"));
        server.verify();
    }

    @Test
    void returnsEmptyWhenTheApiFails() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.frankfurter.dev");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.frankfurter.dev/v1/latest?base=MXN&symbols=USD"))
                .andRespond(withServerError());

        FrankfurterExchangeRateClient client = new FrankfurterExchangeRateClient(builder.build());

        assertThat(client.fetchRate("MXN", "USD")).isEmpty();
        server.verify();
    }

    @Test
    void returnsEmptyWhenTheTargetCurrencyIsMissing() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.frankfurter.dev");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.frankfurter.dev/v1/latest?base=MXN&symbols=USD"))
                .andRespond(withSuccess(
                        "{\"base\":\"MXN\",\"date\":\"2026-10-14\",\"rates\":{\"EUR\":0.05}}",
                        MediaType.APPLICATION_JSON));

        FrankfurterExchangeRateClient client = new FrankfurterExchangeRateClient(builder.build());

        assertThat(client.fetchRate("MXN", "USD")).isEmpty();
        server.verify();
    }
}
