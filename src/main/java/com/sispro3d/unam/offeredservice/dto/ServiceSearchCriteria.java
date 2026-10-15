package com.sispro3d.unam.offeredservice.dto;

import java.math.BigDecimal;

public record ServiceSearchCriteria(
        String query,
        Long categoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer maxDeliveryDays,
        ServiceSortOrder sort
) {

    public ServiceSortOrder sortOrNewest() {
        return sort == null ? ServiceSortOrder.NEWEST : sort;
    }
}
