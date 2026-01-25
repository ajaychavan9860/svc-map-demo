package com.demo.microservices.inventory.service;

import com.demo.microservices.inventory.model.Inventory;
import com.demo.microservices.inventory.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {
    
    @Autowired
    private InventoryRepository inventoryRepository;
    
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }
    
    public Inventory getInventoryById(Long id) {
        return inventoryRepository.findById(id).orElse(null);
    }
    
    public Inventory getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }
    
    public List<Inventory> getLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }
    
    public boolean isStockAvailable(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        return inventory != null && inventory.getAvailableQuantity() >= quantity;
    }
    
    public boolean reserveStock(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        if (inventory != null && inventory.getAvailableQuantity() >= quantity) {
            inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
            inventoryRepository.save(inventory);
            return true;
        }
        return false;
    }
    
    public Inventory saveInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }
    
    public Inventory updateStock(Long id, Integer newQuantity) {
        Inventory inventory = inventoryRepository.findById(id).orElse(null);
        if (inventory != null) {
            inventory.setQuantity(newQuantity);
            return inventoryRepository.save(inventory);
        }
        return null;
    }
}