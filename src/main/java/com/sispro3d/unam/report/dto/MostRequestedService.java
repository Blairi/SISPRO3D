package com.sispro3d.unam.report.dto;

public record MostRequestedService(
        Long serviceId,
        String title,
        String categoryName,
        String expertName,
        long quoteCount,
        long orderCount
) {
}
