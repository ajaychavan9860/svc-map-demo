package com.demo.microservices.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Feign client to test detection of /v1/rawMessage endpoint
 * This simulates ccg-kafka-consumer's CcgCoreServiceProxy
 */
@FeignClient(name = "product-service", url = "${product.service.url}")
public interface ProductTestClient {
    
    @PostMapping("/v1/rawMessage")
    String sendRawMessage();
}
