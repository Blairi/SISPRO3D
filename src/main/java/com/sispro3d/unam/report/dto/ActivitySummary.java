package com.sispro3d.unam.report.dto;

import java.util.Map;

public record ActivitySummary(
        Map<String, Long> accountsByRole,
        Map<String, Long> servicesByStatus,
        Map<String, Long> quotesByStatus,
        Map<String, Long> ordersByStatus,
        long reviewCount
) {
}
