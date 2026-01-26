package com.example.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client to call User Service
 * order-service -> user-service
 */
@FeignClient(name = "${feign.user.name}", url = "${feign.user.url}")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{userId}")
    UserDto getUserById(@PathVariable Long userId);
    
    @GetMapping("/api/users/email/{email}")
    UserDto getUserByEmail(@PathVariable String email);
}

class UserDto {
    private Long id;
    private String name;
    private String email;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
