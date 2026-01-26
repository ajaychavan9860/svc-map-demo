package com.demo.microservices.product.controller;

import org.springframework.web.bind.annotation.*;

@RestController
public class TestEndpointController {
    
    // Simple test endpoint
    @PostMapping("/v1/rawMessage")
    public String testEndpoint() {
        return "test";
    }
}
