package com.demo.microservices.order.client;

import org.springframework.stereotype.Service;

/**
 * This simulates ccg-kafka-consumer-service calling ccg-core-service
 * The string "/v1/rawMessage" appears here, so analyzer should detect:
 * order-service -> product-service dependency
 */
@Service
public class TestClient {
    
    public void callTestEndpoint() {
        // This string matches the endpoint in product-service
        String endpoint = "/v1/rawMessage";
        System.out.println("Calling " + endpoint);
    }
}
