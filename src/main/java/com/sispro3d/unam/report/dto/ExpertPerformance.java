package com.sispro3d.unam.report.dto;

import java.util.Map;

public record ExpertPerformance(
        Long expertId,
        String expertName,
        long serviceCount,
        long answeredQuotes,
        long completedOrders,
        Map<String, Long> ordersByStatus,
        Double averageRating,
        long reviewCount
) {
}
