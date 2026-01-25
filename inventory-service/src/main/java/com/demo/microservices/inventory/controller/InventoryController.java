package com.demo.microservices.inventory.controller;

import com.demo.microservices.inventory.dto.StockUpdateRequest;
import com.demo.microservices.inventory.model.Inventory;
import com.demo.microservices.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    
    @Autowired
    private InventoryService inventoryService;
    
    @GetMapping
    public List<Inventory> getAllInventory() {
        return inventoryService.getAllInventory();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable Long id) {
        Inventory inventory = inventoryService.getInventoryById(id);
        return inventory != null ? ResponseEntity.ok(inventory) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<Inventory> getInventoryByProductId(@PathVariable Long productId) {
        Inventory inventory = inventoryService.getInventoryByProductId(productId);
        return inventory != null ? ResponseEntity.ok(inventory) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/check/{productId}/{quantity}")
    public ResponseEntity<Boolean> checkStock(@PathVariable Long productId, @PathVariable Integer quantity) {
        boolean available = inventoryService.isStockAvailable(productId, quantity);
        return ResponseEntity.ok(available);
    }
    
    @GetMapping("/low-stock")
    public List<Inventory> getLowStockItems() {
        return inventoryService.getLowStockItems();
    }
    
    @PostMapping
    public Inventory createInventory(@RequestBody Inventory inventory) {
        return inventoryService.saveInventory(inventory);
    }
    
    @PutMapping("/{id}/stock")
    public ResponseEntity<Inventory> updateStock(@PathVariable Long id, @RequestBody StockUpdateRequest request) {
        Inventory updated = inventoryService.updateStock(id, request.getQuantity());
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }
    
    @PutMapping("/reserve/{productId}/{quantity}")
    public ResponseEntity<Boolean> reserveStock(@PathVariable Long productId, @PathVariable Integer quantity) {
        boolean reserved = inventoryService.reserveStock(productId, quantity);
        return ResponseEntity.ok(reserved);
    }
}