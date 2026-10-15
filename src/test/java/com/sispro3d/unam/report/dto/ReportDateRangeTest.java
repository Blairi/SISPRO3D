package com.sispro3d.unam.report.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReportDateRangeTest {

    @Test
    void all_includesAnyRecord() {
        ReportDateRange range = ReportDateRange.all();

        assertThat(range.invalid()).isFalse();
        assertThat(range.includes(LocalDateTime.of(2000, 1, 1, 0, 0))).isTrue();
        assertThat(range.includes(LocalDateTime.of(2099, 12, 31, 23, 59))).isTrue();
    }

    @Test
    void fromOnly_includesFromDateOnwards() {
        ReportDateRange range = ReportDateRange.of(LocalDate.of(2026, 2, 1), null);

        assertThat(range.includes(LocalDateTime.of(2026, 1, 31, 23, 59))).isFalse();
        assertThat(range.includes(LocalDateTime.of(2026, 2, 1, 0, 0))).isTrue();
        assertThat(range.includes(LocalDateTime.of(2030, 1, 1, 12, 0))).isTrue();
    }

    @Test
    void toOnly_includesUpToDateInclusive() {
        ReportDateRange range = ReportDateRange.of(null, LocalDate.of(2026, 2, 28));

        assertThat(range.includes(LocalDateTime.of(2026, 2, 28, 23, 59, 59))).isTrue();
        assertThat(range.includes(LocalDateTime.of(2026, 3, 1, 0, 0))).isFalse();
    }

    @Test
    void both_includesOnlyWithinRangeInclusive() {
        ReportDateRange range = ReportDateRange.of(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));

        assertThat(range.includes(LocalDateTime.of(2026, 1, 31, 23, 59))).isFalse();
        assertThat(range.includes(LocalDateTime.of(2026, 2, 1, 0, 0))).isTrue();
        assertThat(range.includes(LocalDateTime.of(2026, 2, 15, 12, 0))).isTrue();
        assertThat(range.includes(LocalDateTime.of(2026, 2, 28, 23, 59, 59))).isTrue();
        assertThat(range.includes(LocalDateTime.of(2026, 3, 1, 0, 0))).isFalse();
    }

    @Test
    void invertedRange_isInvalidAndFallsBackToAllTime() {
        ReportDateRange range = ReportDateRange.of(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 2, 1));

        assertThat(range.invalid()).isTrue();
        assertThat(range.from()).isNull();
        assertThat(range.to()).isNull();
        assertThat(range.includes(LocalDateTime.of(2000, 1, 1, 0, 0))).isTrue();
        assertThat(range.includes(LocalDateTime.of(2099, 12, 31, 23, 59))).isTrue();
    }

    @Test
    void nullCreatedAt_isExcludedWhenABoundIsPresent() {
        assertThat(ReportDateRange.of(LocalDate.of(2026, 2, 1), null).includes(null)).isFalse();
        assertThat(ReportDateRange.of(null, LocalDate.of(2026, 2, 28)).includes(null)).isFalse();
        assertThat(ReportDateRange.all().includes(null)).isTrue();
    }
}
