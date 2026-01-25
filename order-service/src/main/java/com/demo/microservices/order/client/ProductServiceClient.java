package com.demo.microservices.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.math.BigDecimal;

@FeignClient(name = "product-service")
public interface ProductServiceClient {
    
    @GetMapping("/api/products/{id}")
    ProductDto getProductById(@PathVariable("id") Long id);
    
    class ProductDto {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private String category;
        private String brand;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
    }
}