package com.demo.microservices.order.client;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * This class simulates calling the special endpoint on product-service.
 * The analyzer should detect: order-service -> product-service via endpoint "/api/v1/specialProduct"
 */
@Service
public class SpecialProductClient {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    private static final String PRODUCT_SERVICE_URL = "http://product-service:8080";
    
    public void createSpecialProduct(String productData) {
        // Match what the analyzer ACTUALLY extracted (with base path concatenation bug)
        String url = PRODUCT_SERVICE_URL + "/api/products/api/v1/specialProduct";
        restTemplate.postForEntity(url, productData, String.class);
    }
}
