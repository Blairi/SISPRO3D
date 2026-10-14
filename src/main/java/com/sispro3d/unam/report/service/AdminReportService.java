package com.sispro3d.unam.report.service;

import com.sispro3d.unam.report.dto.ActivitySummary;
import com.sispro3d.unam.report.dto.ExpertPerformance;
import com.sispro3d.unam.report.dto.MostRequestedService;
import com.sispro3d.unam.report.dto.ReportDateRange;

import java.util.List;

public interface AdminReportService {

    List<MostRequestedService> mostRequestedServices(ReportDateRange range);

    List<ExpertPerformance> expertPerformance(ReportDateRange range);

    ActivitySummary activitySummary(ReportDateRange range);
}
