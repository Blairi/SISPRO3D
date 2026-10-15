package com.sispro3d.unam.external.exchangerate;

import java.math.BigDecimal;
import java.util.Optional;

public interface ExchangeRateClient {

    Optional<BigDecimal> fetchRate(String base, String target);
}
