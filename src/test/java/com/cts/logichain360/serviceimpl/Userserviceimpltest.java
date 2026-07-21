package com.cts.logichain360.serviceimpl;

import com.cts.logichain360.config.JWTUtil;
import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.dto.response.*;
import com.cts.logichain360.entity.*;
import com.cts.logichain360.enums.UserRole;
import com.cts.logichain360.enums.UserStatus;
import com.cts.logichain360.exception.UserAlreadyExistsException;
import com.cts.logichain360.repository.*;
import com.cts.logichain360.service.impl.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    // Field names must exactly match UserServiceImpl fields
    @Mock private UserRepository userRepo;
    @Mock private CustomerRepository customerRepo;
    @Mock private VendorRepository vendorRepo;
    @Mock private WarehouseManagerRepository wmRepo;
    @Mock private DriverRepository driverRepo;
    @Mock private PasswordEncoder encoder;
    @Mock private JWTUtil jwtutil;   // lowercase 'u' — matches field in UserServiceImpl

    @InjectMocks private UserServiceImpl userService;

    private User customerUser;
    private User driverUser;
    private User vendorUser;
    private User wmUser;
    private User adminUser;

    private UserRegistrationRequest regRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        customerUser = User.builder()
                .id(1L).name("John Doe").phone("9876543210")
                .password("encodedPass").role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE).build();

        driverUser = User.builder()
                .id(2L).name("Ravi Kumar").phone("9876543211")
                .password("encodedPass").role(UserRole.DRIVER)
                .status(UserStatus.ACTIVE).build();

        vendorUser = User.builder()
                .id(3L).name("Priya Vendor").phone("9876543212")
                .password("encodedPass").role(UserRole.VENDOR)
                .status(UserStatus.ACTIVE).build();

        wmUser = User.builder()
                .id(4L).name("Mohan Manager").phone("9876543213")
                .password("encodedPass").role(UserRole.WAREHOUSE_MANAGER)
                .status(UserStatus.ACTIVE).build();

        adminUser = User.builder()
                .id(5L).name("Admin User").phone("9876543214")
                .password("encodedPass").role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE).build();

        regRequest = UserRegistrationRequest.builder()
                .name("John Doe").phone("9876543210")
                .password("Password@123").role(UserRole.CUSTOMER)
                .email("john@example.com").build();

        loginRequest = LoginRequest.builder()
                .phone("9876543210").password("Password@123").build();
    }

    // ─── registerUser ─────────────────────────────────────────────────────────

    @Test
    void registerUser_ShouldReturnCreated_WhenRoleIsCustomer() {
        Customer savedCustomer = Customer.builder().id(101L).user(customerUser).build();

        when(userRepo.existsByPhone("9876543210")).thenReturn(false);
        when(encoder.encode("Password@123")).thenReturn("encodedPass");
        when(userRepo.save(any(User.class))).thenReturn(customerUser);
        when(customerRepo.save(any(Customer.class))).thenReturn(savedCustomer);

        ResponseEntity<UserRegistrationResponse> response = userService.registerUser(regRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getUserId());
        assertEquals("John Doe", response.getBody().getName());
        assertEquals(UserRole.CUSTOMER, response.getBody().getRole());
        assertEquals(101L, response.getBody().getRoleProfileId());
        assertEquals("customers", response.getBody().getRoleProfileTable());
        verify(customerRepo).save(any(Customer.class));
    }

    @Test
    void registerUser_ShouldThrowException_WhenPhoneAlreadyExists() {
        when(userRepo.existsByPhone("9876543210")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.registerUser(regRequest));

        verify(userRepo, never()).save(any());
        verify(customerRepo, never()).save(any());
    }

    @Test
    void registerUser_ShouldCreateVendorProfile_WhenRoleIsVendor() {
        regRequest.setRole(UserRole.VENDOR);
        Vendor savedVendor = Vendor.builder().id(201L).user(vendorUser).build();

        when(userRepo.existsByPhone(any())).thenReturn(false);
        when(encoder.encode(any())).thenReturn("encodedPass");
        when(userRepo.save(any())).thenReturn(vendorUser);
        when(vendorRepo.save(any())).thenReturn(savedVendor);

        ResponseEntity<UserRegistrationResponse> response = userService.registerUser(regRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(201L, response.getBody().getRoleProfileId());
        assertEquals("vendors", response.getBody().getRoleProfileTable());
        verify(vendorRepo).save(any(Vendor.class));
    }

    @Test
    void registerUser_ShouldCreateDriverProfile_WhenRoleIsDriver() {
        regRequest.setRole(UserRole.DRIVER);
        Driver savedDriver = Driver.builder().id(301L).user(driverUser).build();

        when(userRepo.existsByPhone(any())).thenReturn(false);
        when(encoder.encode(any())).thenReturn("encodedPass");
        when(userRepo.save(any())).thenReturn(driverUser);
        when(driverRepo.save(any())).thenReturn(savedDriver);

        ResponseEntity<UserRegistrationResponse> response = userService.registerUser(regRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(301L, response.getBody().getRoleProfileId());
        assertEquals("drivers", response.getBody().getRoleProfileTable());
        verify(driverRepo).save(any(Driver.class));
    }

    @Test
    void registerUser_ShouldCreateWarehouseManagerProfile_WhenRoleIsWarehouseManager() {
        regRequest.setRole(UserRole.WAREHOUSE_MANAGER);
        WarehouseManager savedWm = WarehouseManager.builder().id(401L).user(wmUser).build();

        when(userRepo.existsByPhone(any())).thenReturn(false);
        when(encoder.encode(any())).thenReturn("encodedPass");
        when(userRepo.save(any())).thenReturn(wmUser);
        when(wmRepo.save(any())).thenReturn(savedWm);

        ResponseEntity<UserRegistrationResponse> response = userService.registerUser(regRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(401L, response.getBody().getRoleProfileId());
        assertEquals("warehouse_managers", response.getBody().getRoleProfileTable());
        verify(wmRepo).save(any(WarehouseManager.class));
    }

    @Test
    void registerUser_ShouldReturnNullProfileFields_WhenRoleIsAdmin() {
        regRequest.setRole(UserRole.ADMIN);

        when(userRepo.existsByPhone(any())).thenReturn(false);
        when(encoder.encode(any())).thenReturn("encodedPass");
        when(userRepo.save(any())).thenReturn(adminUser);

        ResponseEntity<UserRegistrationResponse> response = userService.registerUser(regRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNull(response.getBody().getRoleProfileId());
        assertNull(response.getBody().getRoleProfileTable());
        // No profile repo should be called for ADMIN
        verify(customerRepo, never()).save(any());
        verify(vendorRepo, never()).save(any());
        verify(driverRepo, never()).save(any());
        verify(wmRepo, never()).save(any());
    }

    // ─── login ────────────────────────────────────────────────────────────────

    @Test
    void login_ShouldReturnTokenAndUserDetails_WhenCredentialsAreValid() {
        when(userRepo.findByPhoneAndIsDeletedFalse("9876543210"))
                .thenReturn(Optional.of(customerUser));
        when(encoder.matches("Password@123", "encodedPass")).thenReturn(true);
        when(jwtutil.generateToken("9876543210")).thenReturn("jwt.token.here");

        ResponseEntity<LoginResponse> response = userService.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt.token.here", response.getBody().getToken());
        assertEquals(1L, response.getBody().getUserId());
        assertEquals("John Doe", response.getBody().getName());
        assertEquals("9876543210", response.getBody().getPhone());
        assertEquals(UserRole.CUSTOMER, response.getBody().getRole());
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenPasswordIsWrong() {
        when(userRepo.findByPhoneAndIsDeletedFalse("9876543210"))
                .thenReturn(Optional.of(customerUser));
        when(encoder.matches("Password@123", "encodedPass")).thenReturn(false);

        ResponseEntity<LoginResponse> response = userService.login(loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(jwtutil, never()).generateToken(any());
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenUserNotFound() {
        when(userRepo.findByPhoneAndIsDeletedFalse("9876543210"))
                .thenReturn(Optional.empty());

        ResponseEntity<LoginResponse> response = userService.login(loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(encoder, never()).matches(any(), any());
    }

    // ─── getUserById ──────────────────────────────────────────────────────────

    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(customerUser));

        ResponseEntity<UserResponse> response = userService.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        assertEquals("John Doe", response.getBody().getName());
        assertEquals("9876543210", response.getBody().getPhone());
        assertEquals(UserRole.CUSTOMER, response.getBody().getRole());
        assertEquals(UserStatus.ACTIVE, response.getBody().getStatus());
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenDoesNotExist() {
        when(userRepo.findById(999L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, userService.getUserById(999L).getStatusCode());
    }

    // ─── getAllUsers ──────────────────────────────────────────────────────────

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        when(userRepo.findAll()).thenReturn(Arrays.asList(customerUser, driverUser));

        ResponseEntity<List<UserResponse>> response = userService.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("John Doe", response.getBody().get(0).getName());
        assertEquals("Ravi Kumar", response.getBody().get(1).getName());
    }

    @Test
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsersExist() {
        when(userRepo.findAll()).thenReturn(List.of());

        ResponseEntity<List<UserResponse>> response = userService.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // ─── updateUser ───────────────────────────────────────────────────────────

    @Test
    void updateUser_ShouldUpdateName_WhenNameProvided() {
        UpdateUserRequest req = UpdateUserRequest.builder().name("John Updated").build();
        when(userRepo.findById(1L)).thenReturn(Optional.of(customerUser));
        when(userRepo.save(any())).thenReturn(customerUser);

        ResponseEntity<UserResponse> response = userService.updateUser(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Updated", customerUser.getName());
        verify(userRepo).save(customerUser);
    }

    @Test
    void updateUser_ShouldEncodeAndUpdatePassword_WhenPasswordProvided() {
        UpdateUserRequest req = UpdateUserRequest.builder().password("NewPass@123").build();
        when(userRepo.findById(1L)).thenReturn(Optional.of(customerUser));
        when(encoder.encode("NewPass@123")).thenReturn("newEncodedPass");
        when(userRepo.save(any())).thenReturn(customerUser);

        userService.updateUser(1L, req);

        assertEquals("newEncodedPass", customerUser.getPassword());
        verify(encoder).encode("NewPass@123");
    }

    @Test
    void updateUser_ShouldUpdatePhone_WhenPhoneIsNewAndNotTaken() {
        UpdateUserRequest req = UpdateUserRequest.builder().phone("9999999999").build();
        when(userRepo.findById(1L)).thenReturn(Optional.of(customerUser));
        when(userRepo.existsByPhone("9999999999")).thenReturn(false);
        when(userRepo.save(any())).thenReturn(customerUser);

        userService.updateUser(1L, req);

        assertEquals("9999999999", customerUser.getPhone());
    }

    @Test
    void updateUser_ShouldNotUpdatePhone_WhenSamePhoneProvided() {
        // phone is same as current — should skip existsByPhone check entirely
        UpdateUserRequest req = UpdateUserRequest.builder().phone("9876543210").build();
        when(userRepo.findById(1L)).thenReturn(Optional.of(customerUser));
        when(userRepo.save(any())).thenReturn(customerUser);

        userService.updateUser(1L, req);

        verify(userRepo, never()).existsByPhone(any());
    }

    @Test
    void updateUser_ShouldThrowException_WhenNewPhoneAlreadyTakenByAnotherUser() {
        UpdateUserRequest req = UpdateUserRequest.builder().phone("9999999999").build();
        when(userRepo.findById(1L)).thenReturn(Optional.of(customerUser));
        when(userRepo.existsByPhone("9999999999")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.updateUser(1L, req));
        verify(userRepo, never()).save(any());
    }

    @Test
    void updateUser_ShouldNotUpdateNullFields() {
        UpdateUserRequest req = UpdateUserRequest.builder().build(); // all null
        when(userRepo.findById(1L)).thenReturn(Optional.of(customerUser));
        when(userRepo.save(any())).thenReturn(customerUser);

        userService.updateUser(1L, req);

        // original values unchanged
        assertEquals("John Doe", customerUser.getName());
        assertEquals("encodedPass", customerUser.getPassword());
        verify(encoder, never()).encode(any());
    }

    @Test
    void updateUser_ShouldReturnNotFound_WhenUserDoesNotExist() {
        when(userRepo.findById(999L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND,
                userService.updateUser(999L, UpdateUserRequest.builder().build()).getStatusCode());
        verify(userRepo, never()).save(any());
    }

    // ─── updateUserStatus ─────────────────────────────────────────────────────

    @Test
    void updateUserStatus_ShouldSetStatusInactive_WhenUserExists() {
        UserStatusRequest req = UserStatusRequest.builder().status(UserStatus.INACTIVE).build();
        when(userRepo.findById(1L)).thenReturn(Optional.of(customerUser));
        when(userRepo.save(any())).thenReturn(customerUser);

        ResponseEntity<UserResponse> response = userService.updateUserStatus(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(UserStatus.INACTIVE, customerUser.getStatus());
        verify(userRepo).save(customerUser);
    }

    @Test
    void updateUserStatus_ShouldReturnNotFound_WhenUserDoesNotExist() {
        when(userRepo.findById(999L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND,
                userService.updateUserStatus(999L,
                        UserStatusRequest.builder().status(UserStatus.INACTIVE).build())
                        .getStatusCode());
        verify(userRepo, never()).save(any());
    }

    // ─── deleteUser ───────────────────────────────────────────────────────────
    // Actual code uses switch(user.getRole()) and calls findByUser().ifPresent(repo::delete)

    @Test
    void deleteUser_ShouldDeleteCustomerProfile_WhenUserIsCustomer() {
        Customer mockCustomer = Customer.builder().id(1L).user(customerUser).build();
        when(userRepo.findById(1L)).thenReturn(Optional.of(customerUser));
        when(customerRepo.findByUser(customerUser)).thenReturn(Optional.of(mockCustomer));

        ResponseEntity<Void> response = userService.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(customerRepo).delete(mockCustomer);
        verify(userRepo).delete(customerUser);
        // Other repos must NOT be called for CUSTOMER role
        verify(driverRepo, never()).findByUser(any());
        verify(vendorRepo, never()).findByUser(any());
        verify(wmRepo, never()).findByUser(any());
    }

    @Test
    void deleteUser_ShouldDeleteDriverProfile_WhenUserIsDriver() {
        Driver mockDriver = Driver.builder().id(1L).user(driverUser).build();
        when(userRepo.findById(2L)).thenReturn(Optional.of(driverUser));
        when(driverRepo.findByUser(driverUser)).thenReturn(Optional.of(mockDriver));

        ResponseEntity<Void> response = userService.deleteUser(2L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(driverRepo).delete(mockDriver);
        verify(userRepo).delete(driverUser);
        verify(customerRepo, never()).findByUser(any());
    }

    @Test
    void deleteUser_ShouldDeleteVendorProfile_WhenUserIsVendor() {
        Vendor mockVendor = Vendor.builder().id(1L).user(vendorUser).build();
        when(userRepo.findById(3L)).thenReturn(Optional.of(vendorUser));
        when(vendorRepo.findByUser(vendorUser)).thenReturn(Optional.of(mockVendor));

        ResponseEntity<Void> response = userService.deleteUser(3L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(vendorRepo).delete(mockVendor);
        verify(userRepo).delete(vendorUser);
        verify(customerRepo, never()).findByUser(any());
    }

    @Test
    void deleteUser_ShouldDeleteWarehouseManagerProfile_WhenUserIsWarehouseManager() {
        WarehouseManager mockWm = WarehouseManager.builder().id(1L).user(wmUser).build();
        when(userRepo.findById(4L)).thenReturn(Optional.of(wmUser));
        when(wmRepo.findByUser(wmUser)).thenReturn(Optional.of(mockWm));

        ResponseEntity<Void> response = userService.deleteUser(4L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(wmRepo).delete(mockWm);
        verify(userRepo).delete(wmUser);
        verify(customerRepo, never()).findByUser(any());
    }

    @Test
    void deleteUser_ShouldDeleteUserOnly_WhenUserIsAdmin() {
        // ADMIN has no profile table — switch case just deletes the user
        when(userRepo.findById(5L)).thenReturn(Optional.of(adminUser));

        ResponseEntity<Void> response = userService.deleteUser(5L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userRepo).delete(adminUser);
        // No profile repo should be touched for ADMIN
        verify(customerRepo, never()).findByUser(any());
        verify(driverRepo, never()).findByUser(any());
        verify(vendorRepo, never()).findByUser(any());
        verify(wmRepo, never()).findByUser(any());
    }

    @Test
    void deleteUser_ShouldNotDeleteProfile_WhenProfileDoesNotExist() {
        // Customer user but no customer profile row found
        when(userRepo.findById(1L)).thenReturn(Optional.of(customerUser));
        when(customerRepo.findByUser(customerUser)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = userService.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(customerRepo, never()).delete(any());
        verify(userRepo).delete(customerUser); // user still deleted
    }

    @Test
    void deleteUser_ShouldReturnNotFound_WhenUserDoesNotExist() {
        when(userRepo.findById(999L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, userService.deleteUser(999L).getStatusCode());
        verify(userRepo, never()).delete(any());
        verify(customerRepo, never()).findByUser(any());
    }
}