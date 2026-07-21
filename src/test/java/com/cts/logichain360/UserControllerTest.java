package com.cts.logichain360;

import com.cts.logichain360.controller.UserController;
import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.dto.response.*;
import com.cts.logichain360.enums.UserRole;
import com.cts.logichain360.enums.UserStatus;
import com.cts.logichain360.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UserController using Mockito.
 * Covers: register, login, getById, getAll, update, updateStatus, delete
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    // ─── Test Data ────────────────────────────────────────────────────────────

    private UserRegistrationRequest registrationRequest;
    private UserRegistrationResponse registrationResponse;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private UserResponse userResponse;
    private UpdateUserRequest updateUserRequest;
    private UserStatusRequest userStatusRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

        // Registration request
        registrationRequest = UserRegistrationRequest.builder()
                .name("John Doe")
                .phone("9876543210")
                .password("Password@123")
                .role(UserRole.CUSTOMER)
                .email("john@example.com")
                .build();

        // Registration response
        registrationResponse = UserRegistrationResponse.builder()
                .userId(1L)
                .name("John Doe")
                .phone("9876543210")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .roleProfileId(101L)
                .roleProfileTable("customers")
                .build();

        // Login request / response
        loginRequest = LoginRequest.builder()
                .phone("9876543210")
                .password("Password@123")
                .build();

        loginResponse = LoginResponse.builder()
                .token("eyJhbGciOiJIUzI1NiJ9.token")
                .userId(1L)
                .name("John Doe")
                .phone("9876543210")
                .role(UserRole.CUSTOMER)
                .build();

        // Generic user response
        userResponse = UserResponse.builder()
                .id(1L)
                .name("John Doe")
                .phone("9876543210")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        // Update request
        updateUserRequest = UpdateUserRequest.builder()
                .name("John Updated")
                .phone("9876543211")
                .build();

        // Status request
        userStatusRequest = UserStatusRequest.builder()
                .status(UserStatus.INACTIVE)
                .build();
    }

    // ─── Register Tests ───────────────────────────────────────────────────────

    @Test
    void register_ShouldReturnCreatedUser_WhenValidRequest() {
        // Arrange
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(registrationResponse));

        // Act
        ResponseEntity<UserRegistrationResponse> response = userController.register(registrationRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getUserId());
        assertEquals("John Doe", response.getBody().getName());
        assertEquals(UserRole.CUSTOMER, response.getBody().getRole());
        assertEquals(UserStatus.ACTIVE, response.getBody().getStatus());

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    void register_ShouldReturnOk_WhenServiceReturnsOk() {
        // Arrange
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenReturn(ResponseEntity.ok(registrationResponse));

        // Act
        ResponseEntity<UserRegistrationResponse> response = userController.register(registrationRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).registerUser(registrationRequest);
    }

    @Test
    void register_ShouldDelegateToService_WithCorrectRequest() {
        // Arrange
        when(userService.registerUser(registrationRequest))
                .thenReturn(ResponseEntity.ok(registrationResponse));

        // Act
        userController.register(registrationRequest);

        // Assert - verify exact request object is passed
        verify(userService).registerUser(registrationRequest);
        verifyNoMoreInteractions(userService);
    }

    // ─── Login Tests ──────────────────────────────────────────────────────────

    @Test
    void login_ShouldReturnTokenAndUserDetails_WhenValidCredentials() {
        // Arrange
        when(userService.login(any(LoginRequest.class)))
                .thenReturn(ResponseEntity.ok(loginResponse));

        // Act
        ResponseEntity<LoginResponse> response = userController.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
        assertEquals("John Doe", response.getBody().getName());
        assertEquals(UserRole.CUSTOMER, response.getBody().getRole());

        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenInvalidCredentials() {
        // Arrange
        when(userService.login(any(LoginRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());

        // Act
        ResponseEntity<LoginResponse> response = userController.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void login_ShouldPassCorrectCredentialsToService() {
        // Arrange
        when(userService.login(loginRequest)).thenReturn(ResponseEntity.ok(loginResponse));

        // Act
        userController.login(loginRequest);

        // Assert
        verify(userService).login(loginRequest);
    }

    // ─── GetById Tests ────────────────────────────────────────────────────────

    @Test
    void getById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(ResponseEntity.ok(userResponse));

        // Act
        ResponseEntity<UserResponse> response = userController.getById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("John Doe", response.getBody().getName());

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void getById_ShouldReturnNotFound_WhenUserDoesNotExist() {
        // Arrange
        when(userService.getUserById(999L)).thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<UserResponse> response = userController.getById(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).getUserById(999L);
    }

    @Test
    void getById_ShouldCallServiceWithCorrectId() {
        // Arrange
        Long userId = 42L;
        when(userService.getUserById(userId)).thenReturn(ResponseEntity.ok(userResponse));

        // Act
        userController.getById(userId);

        // Assert
        verify(userService).getUserById(userId);
        verify(userService, never()).getUserById(argThat(id -> !id.equals(userId)));
    }

    // ─── GetAll Tests ─────────────────────────────────────────────────────────

    @Test
    void getAll_ShouldReturnListOfUsers_WhenUsersExist() {
        // Arrange
        UserResponse user2 = UserResponse.builder()
                .id(2L).name("Jane Doe").phone("9876543212")
                .role(UserRole.DRIVER).status(UserStatus.ACTIVE).build();

        List<UserResponse> users = Arrays.asList(userResponse, user2);
        when(userService.getAllUsers()).thenReturn(ResponseEntity.ok(users));

        // Act
        ResponseEntity<List<UserResponse>> response = userController.getAll();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNoUsersExist() {
        // Arrange
        when(userService.getAllUsers()).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        // Act
        ResponseEntity<List<UserResponse>> response = userController.getAll();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    // ─── Update Tests ─────────────────────────────────────────────────────────

    @Test
    void update_ShouldReturnUpdatedUser_WhenValidRequest() {
        // Arrange
        UserResponse updatedResponse = UserResponse.builder()
                .id(1L).name("John Updated").phone("9876543211")
                .role(UserRole.CUSTOMER).status(UserStatus.ACTIVE).build();

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class)))
                .thenReturn(ResponseEntity.ok(updatedResponse));

        // Act
        ResponseEntity<UserResponse> response = userController.update(1L, updateUserRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John Updated", response.getBody().getName());

        verify(userService, times(1)).updateUser(eq(1L), any(UpdateUserRequest.class));
    }

    @Test
    void update_ShouldReturnNotFound_WhenUserDoesNotExist() {
        // Arrange
        when(userService.updateUser(eq(999L), any(UpdateUserRequest.class)))
                .thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<UserResponse> response = userController.update(999L, updateUserRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── UpdateStatus Tests ───────────────────────────────────────────────────

    @Test
    void updateStatus_ShouldReturnUpdatedUser_WhenStatusChanged() {
        // Arrange
        UserResponse inactiveUser = UserResponse.builder()
                .id(1L).name("John Doe").phone("9876543210")
                .role(UserRole.CUSTOMER).status(UserStatus.INACTIVE).build();

        when(userService.updateUserStatus(eq(1L), any(UserStatusRequest.class)))
                .thenReturn(ResponseEntity.ok(inactiveUser));

        // Act
        ResponseEntity<UserResponse> response = userController.updateStatus(1L, userStatusRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(UserStatus.INACTIVE, response.getBody().getStatus());

        verify(userService, times(1)).updateUserStatus(eq(1L), any(UserStatusRequest.class));
    }

    @Test
    void updateStatus_ShouldPassCorrectIdAndRequest() {
        // Arrange
        when(userService.updateUserStatus(1L, userStatusRequest))
                .thenReturn(ResponseEntity.ok(userResponse));

        // Act
        userController.updateStatus(1L, userStatusRequest);

        // Assert
        verify(userService).updateUserStatus(1L, userStatusRequest);
    }

    // ─── Delete Tests ─────────────────────────────────────────────────────────

    @Test
    void delete_ShouldReturnNoContent_WhenUserDeletedSuccessfully() {
        // Arrange
        when(userService.deleteUser(1L)).thenReturn(ResponseEntity.noContent().build());

        // Act
        ResponseEntity<Void> response = userController.delete(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void delete_ShouldReturnNotFound_WhenUserDoesNotExist() {
        // Arrange
        when(userService.deleteUser(999L)).thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<Void> response = userController.delete(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).deleteUser(999L);
    }

    @Test
    void delete_ShouldCallServiceOnceWithCorrectId() {
        // Arrange
        when(userService.deleteUser(anyLong())).thenReturn(ResponseEntity.noContent().build());

        // Act
        userController.delete(5L);

        // Assert
        verify(userService, times(1)).deleteUser(5L);
        verify(userService, never()).deleteUser(argThat(id -> !id.equals(5L)));
    }
    
    
    //Admin test cases
 @Test
 void registerAdmin_succeeds_withNullRoleProfile() {
     UserRegistrationRequest adminRegReq = UserRegistrationRequest.builder()
             .name("Super Admin")
             .phone("9999999999")
             .password("Admin@1234")
             .role(UserRole.ADMIN)
             .build();

     UserRegistrationResponse adminRegResp = UserRegistrationResponse.builder()
             .userId(100L)
             .name("Super Admin")
             .phone("9999999999")
             .role(UserRole.ADMIN)
             .status(UserStatus.ACTIVE)
             .roleProfileId(null)
             .roleProfileTable(null)
             .build();

     when(userService.registerUser(any()))
             .thenReturn(new ResponseEntity<>(adminRegResp, HttpStatus.CREATED));

     ResponseEntity<UserRegistrationResponse> resp = userController.register(adminRegReq);

     assertEquals(HttpStatus.CREATED, resp.getStatusCode());
     assertEquals(UserRole.ADMIN, resp.getBody().getRole());
     assertNull(resp.getBody().getRoleProfileId());
     assertNull(resp.getBody().getRoleProfileTable());
 }

 @Test
 void registerAdmin_nameAndPhoneSet() {
     when(userService.registerUser(any()))
             .thenReturn(new ResponseEntity<>(
                     UserRegistrationResponse.builder()
                             .userId(100L)
                             .name("Super Admin")
                             .phone("9999999999")
                             .role(UserRole.ADMIN)
                             .status(UserStatus.ACTIVE)
                             .build(),
                     HttpStatus.CREATED));

     UserRegistrationResponse body = userController.register(
             UserRegistrationRequest.builder()
                     .name("Super Admin")
                     .phone("9999999999")
                     .password("Admin@1234")
                     .role(UserRole.ADMIN)
                     .build()
     ).getBody();

     assertEquals("Super Admin", body.getName());
     assertEquals("9999999999", body.getPhone());
     assertEquals(UserStatus.ACTIVE, body.getStatus());
 }

 @Test
 void registerAdmin_serviceCalledOnce() {
     UserRegistrationRequest req = UserRegistrationRequest.builder()
             .name("Super Admin")
             .phone("9999999999")
             .password("Admin@1234")
             .role(UserRole.ADMIN)
             .build();

     when(userService.registerUser(any()))
             .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());

     userController.register(req);

     verify(userService, times(1)).registerUser(req);
 }

 @Test
 void loginAdmin_returnsToken() {
     LoginRequest loginReq = LoginRequest.builder()
             .phone("9999999999")
             .password("Admin@1234")
             .build();

     LoginResponse loginResp = LoginResponse.builder()
             .token("jwt.token.here")
             .userId(100L)
             .name("Super Admin")
             .phone("9999999999")
             .role(UserRole.ADMIN)
             .build();

     when(userService.login(any())).thenReturn(ResponseEntity.ok(loginResp));

     ResponseEntity<LoginResponse> resp = userController.login(loginReq);

     assertEquals(HttpStatus.OK, resp.getStatusCode());
     assertEquals(UserRole.ADMIN, resp.getBody().getRole());
     assertNotNull(resp.getBody().getToken());
 }

 @Test
 void deleteAdmin_succeeds_noProfileCleanupNeeded() {
     when(userService.deleteUser(100L))
             .thenReturn(ResponseEntity.noContent().build());

     ResponseEntity<Void> resp = userController.delete(100L);

     assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
     verify(userService).deleteUser(100L);
 }

 @Test
 void registerAdmin_statusIsActiveByDefault() {
     when(userService.registerUser(any()))
             .thenReturn(new ResponseEntity<>(
                     UserRegistrationResponse.builder()
                             .userId(100L)
                             .name("Super Admin")
                             .phone("9999999999")
                             .role(UserRole.ADMIN)
                             .status(UserStatus.ACTIVE)
                             .build(),
                     HttpStatus.CREATED));

     UserRegistrationResponse body = userController.register(
             UserRegistrationRequest.builder()
                     .name("Super Admin")
                     .phone("9999999999")
                     .password("Admin@1234")
                     .role(UserRole.ADMIN)
                     .build()
     ).getBody();

     assertEquals(UserStatus.ACTIVE, body.getStatus());
 }
}