package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.report.dto.ReportDateRange;
import com.sispro3d.unam.report.service.AdminReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reports")
@Slf4j
public class AdminReportsController {

    @Autowired
    private AdminReportService adminReportService;

    @GetMapping
    public String reports(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model) {
        log.debug("GET /admin/reports - desde={}, hasta={}", from, to);

        ReportDateRange range = ReportDateRange.of(from, to);
        if (range.invalid()) {
            model.addAttribute("rangeError",
                    "La fecha \"desde\" no puede ser posterior a la fecha \"hasta\".");
        }

        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("mostRequestedServices", adminReportService.mostRequestedServices(range));
        model.addAttribute("expertPerformance", adminReportService.expertPerformance(range));
        model.addAttribute("activitySummary", adminReportService.activitySummary(range));
        return "admin/reports";
    }
}
