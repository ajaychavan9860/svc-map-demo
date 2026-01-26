package com.example.user.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{userId}")
    public UserResponse getUserById(@PathVariable Long userId) {
        return new UserResponse(userId, "User " + userId, "user" + userId + "@example.com");
    }
    
    @GetMapping("/email/{email}")
    public UserResponse getUserByEmail(@PathVariable String email) {
        return new UserResponse(1L, "User", email);
    }
    
    static class UserResponse {
        private Long id;
        private String name;
        private String email;
        
        public UserResponse(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
        
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }
}
