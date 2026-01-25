package com.demo.microservices.inventory.dto;

public class StockUpdateRequest {
    private Integer quantity;
    
    // Constructors
    public StockUpdateRequest() {}
    
    public StockUpdateRequest(Integer quantity) {
        this.quantity = quantity;
    }
    
    // Getters and Setters
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}