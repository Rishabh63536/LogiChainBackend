package com.cts.logichain360.serviceimpl;

import com.cts.logichain360.entity.User;
import com.cts.logichain360.enums.UserRole;
import com.cts.logichain360.enums.UserStatus;
import com.cts.logichain360.repository.UserRepository;
import com.cts.logichain360.service.impl.UserInfoConfigManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserInfoConfigManagerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserInfoConfigManager userInfoConfigManager;

    // ─── loadUserByUsername ───────────────────────────────────────────────────
    // Actual source code:
    //   User user = userRepository.findByPhone(phone);
    //   .username(user.getPhone())     ← phone is used as the Spring Security username
    //   .password(user.getPassword())
    //   .roles(user.getRole().name())

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WithPhoneAsUsername() {
        User mockUser = User.builder()
                .id(1L)
                .name("John Doe")
                .phone("9876543210")
                .password("encodedPassword")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findByPhone("9876543210")).thenReturn(mockUser);

        UserDetails userDetails = userInfoConfigManager.loadUserByUsername("9876543210");

        assertNotNull(userDetails);
        // username is set to user.getPhone() — NOT user.getName()
        assertEquals("9876543210", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
        verify(userRepository).findByPhone("9876543210");
    }

    @Test
    void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenUserNotFound() {
        when(userRepository.findByPhone("0000000000")).thenReturn(null);

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> userInfoConfigManager.loadUserByUsername("0000000000"));

        assertTrue(ex.getMessage().contains("0000000000"));
        verify(userRepository).findByPhone("0000000000");
    }

    @Test
    void loadUserByUsername_ShouldHaveCorrectRole_ForDriverUser() {
        User driverUser = User.builder()
                .id(2L).name("Ravi Driver").phone("9876543211")
                .password("encoded").role(UserRole.DRIVER)
                .status(UserStatus.ACTIVE).build();

        when(userRepository.findByPhone("9876543211")).thenReturn(driverUser);

        UserDetails userDetails = userInfoConfigManager.loadUserByUsername("9876543211");

        assertEquals("9876543211", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DRIVER")));
    }

    @Test
    void loadUserByUsername_ShouldHaveCorrectRole_ForVendorUser() {
        User vendorUser = User.builder()
                .id(3L).name("Priya Vendor").phone("9876543212")
                .password("encoded").role(UserRole.VENDOR)
                .status(UserStatus.ACTIVE).build();

        when(userRepository.findByPhone("9876543212")).thenReturn(vendorUser);

        UserDetails userDetails = userInfoConfigManager.loadUserByUsername("9876543212");

        assertEquals("9876543212", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VENDOR")));
    }

    @Test
    void loadUserByUsername_ShouldHaveCorrectRole_ForWarehouseManagerUser() {
        User wmUser = User.builder()
                .id(4L).name("Mohan Manager").phone("9876543213")
                .password("encoded").role(UserRole.WAREHOUSE_MANAGER)
                .status(UserStatus.ACTIVE).build();

        when(userRepository.findByPhone("9876543213")).thenReturn(wmUser);

        UserDetails userDetails = userInfoConfigManager.loadUserByUsername("9876543213");

        assertEquals("9876543213", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_WAREHOUSE_MANAGER")));
    }

    @Test
    void loadUserByUsername_ShouldHaveCorrectRole_ForAdminUser() {
        User adminUser = User.builder()
                .id(5L).name("Admin").phone("9876543214")
                .password("encoded").role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE).build();

        when(userRepository.findByPhone("9876543214")).thenReturn(adminUser);

        UserDetails userDetails = userInfoConfigManager.loadUserByUsername("9876543214");

        assertEquals("9876543214", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}