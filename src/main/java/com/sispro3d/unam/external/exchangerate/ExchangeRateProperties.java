package com.sispro3d.unam.external.exchangerate;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "external.exchange-rate")
public class ExchangeRateProperties {

    private boolean enabled = true;

    private String baseUrl = "https://api.frankfurter.dev";

    private String baseCurrency = "MXN";

    private String targetCurrency = "USD";

    private Duration connectTimeout = Duration.ofSeconds(2);

    private Duration readTimeout = Duration.ofSeconds(2);
}
