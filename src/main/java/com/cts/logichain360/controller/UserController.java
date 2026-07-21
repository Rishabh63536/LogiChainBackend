package com.cts.logichain360.controller;

import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.dto.response.*;
import com.cts.logichain360.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users, registration, and authentication")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Register a new user", description = "Registers a new user in the system")
    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponse> register(@RequestBody UserRegistrationRequest request) {
        return userService.registerUser(request);
    }

    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }
    
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their unique ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieves a list of all users")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return userService.getAllUsers();
    }
    
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update a user", description = "Updates an existing user's details by their ID")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user status",
            description = "Updates the account status (e.g., active, inactive) of a specific user")
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateStatus(@PathVariable Long id, @RequestBody UserStatusRequest request) {
        return userService.updateUserStatus(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a user", description = "Soft-deletes a user by their ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return userService.deleteUser(id);
    }
}