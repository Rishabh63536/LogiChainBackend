package com.cts.logichain360.service;

import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.dto.response.*;
import com.cts.logichain360.entity.User;
import com.cts.logichain360.dto.response.UserRegistrationResponse;
import com.cts.logichain360.dto.request.UserRegistrationRequest;
import com.cts.logichain360.dto.request.LoginRequest;

import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Optional;

public interface UserService {
    ResponseEntity<UserRegistrationResponse> registerUser(UserRegistrationRequest request);
    ResponseEntity<LoginResponse> login(LoginRequest request);
    ResponseEntity<UserResponse> getUserById(Long id);
    ResponseEntity<List<UserResponse>> getAllUsers();
    ResponseEntity<UserResponse> updateUser(Long id, UpdateUserRequest request);
    ResponseEntity<UserResponse> updateUserStatus(Long id, UserStatusRequest request);
    ResponseEntity<Void> deleteUser(Long id);
}