package com.example.analyticsservice.controller;

import com.example.analyticsservice.service.BusinessAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private BusinessAnalyticsService analyticsService;

    @GetMapping("/users")
    public String getUserAnalytics() {
        return analyticsService.generateUserAnalytics();
    }

    @GetMapping("/orders")
    public String getOrderAnalytics() {
        return analyticsService.generateOrderAnalytics();
    }

    @GetMapping("/payments")
    public String getPaymentAnalytics() {
        return analyticsService.generatePaymentAnalytics();
    }

    @GetMapping("/inventory")
    public String getInventoryAnalytics() {
        return analyticsService.generateInventoryAnalytics();
    }

    @GetMapping("/dashboard")
    public String getDashboard() {
        return "Analytics Dashboard: " + 
               analyticsService.generateUserAnalytics() + ", " +
               analyticsService.generateOrderAnalytics() + ", " +
               analyticsService.generatePaymentAnalytics() + ", " +
               analyticsService.generateInventoryAnalytics();
    }
}