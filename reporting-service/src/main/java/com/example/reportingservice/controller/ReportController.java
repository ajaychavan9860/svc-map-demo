package com.example.reportingservice.controller;

import com.example.reportingservice.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardReport() {
        return reportService.generateDashboardReport();
    }

    @GetMapping("/health")
    public String health() {
        return "Reporting service is healthy";
    }
}