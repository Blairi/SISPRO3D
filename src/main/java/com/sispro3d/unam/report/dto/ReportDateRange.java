package com.sispro3d.unam.report.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ReportDateRange(LocalDate from, LocalDate to, boolean invalid) {

    public static ReportDateRange all() {
        return new ReportDateRange(null, null, false);
    }

    public static ReportDateRange of(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            return new ReportDateRange(null, null, true);
        }
        return new ReportDateRange(from, to, false);
    }

    public boolean includes(LocalDateTime createdAt) {
        if (createdAt == null) {
            return from == null && to == null;
        }
        if (from != null && createdAt.isBefore(from.atStartOfDay())) {
            return false;
        }
        if (to != null && createdAt.isAfter(to.atTime(LocalTime.MAX))) {
            return false;
        }
        return true;
    }
}
