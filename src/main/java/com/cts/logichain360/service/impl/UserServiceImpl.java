package com.cts.logichain360.service.impl;

import com.cts.logichain360.annotation.Auditable;
import com.cts.logichain360.config.JWTUtil;
import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.dto.response.*;
import com.cts.logichain360.entity.*;
import com.cts.logichain360.exception.UserAlreadyExistsException;
import com.cts.logichain360.enums.AuditAction;
import com.cts.logichain360.enums.UserRole;
import com.cts.logichain360.mapper.UserMapper;
import com.cts.logichain360.repository.*;
import com.cts.logichain360.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final CustomerRepository customerRepo;
    private final VendorRepository vendorRepo;
    private final WarehouseManagerRepository wmRepo;
    private final DriverRepository driverRepo;
    private final PasswordEncoder encoder;
    private final JWTUtil jwtutil;
    private final UserMapper userMapper;

    @Override
    @Transactional
    @Auditable(action = AuditAction.USER_REGISTERED, entityType = "User")
    public ResponseEntity<UserRegistrationResponse> registerUser(UserRegistrationRequest req) {
        if (userRepo.existsByPhone(req.getPhone())) {
            throw new UserAlreadyExistsException("Phone " + req.getPhone() + " is already registered.");
        }

        User saved = userRepo.save(userMapper.toUser(req, encoder.encode(req.getPassword())));

        ProfileResult p = createRoleProfile(saved, req);

        return new ResponseEntity<>(userMapper.toRegistrationResponse(saved, p.id(), p.table()),HttpStatus.CREATED);
    }

    @Override
    @Auditable(action = AuditAction.USER_LOGGED_IN, entityType = "User")
    public ResponseEntity<LoginResponse> login(LoginRequest req) {
    	Optional<User> optionalUser = userRepo.findByPhoneAndIsDeletedFalse(req.getPhone())
    	        .filter(user -> encoder.matches(req.getPassword(), user.getPassword()));

    	if (optionalUser.isEmpty()) {
    	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}

    	User user = optionalUser.get();

    	ProfileResult p = resolveRoleProfile(user);

    	Long warehouseId = user.getRole() == UserRole.WAREHOUSE_MANAGER
    	        ? wmRepo.findByUser(user)
    	                .map(wm -> wm.getAssignedWarehouse() == null
    	                        ? null
    	                        : wm.getAssignedWarehouse().getId())
    	                .orElse(null)
    	        : null;

    	return ResponseEntity.ok(
    	        userMapper.toLoginResponse(
    	                user,
    	                jwtutil.generateToken(user.getPhone()),
    	                p.id(),
    	                p.table(),
    	                warehouseId
    	        )
    	);
    }

    @Override
    public ResponseEntity<UserResponse> getUserById(Long id) {
        return userRepo.findById(id)
                .map(u -> ResponseEntity.ok(userMapper.toUserResponse(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(
                userRepo.findAll().stream().map(userMapper::toUserResponse).toList());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.USER_UPDATED, entityType = "User")
    public ResponseEntity<UserResponse> updateUser(Long id, UpdateUserRequest req) {
        return userRepo.findById(id).map(user -> {
            if (req.getName() != null)
                user.setName(req.getName());
            if (req.getPassword() != null)
                user.setPassword(encoder.encode(req.getPassword()));
            if (req.getPhone() != null && !req.getPhone().equals(user.getPhone())) {
                if (userRepo.existsByPhone(req.getPhone()))
                    throw new UserAlreadyExistsException("Phone " + req.getPhone() + " already in use.");
                user.setPhone(req.getPhone());
            }
            return ResponseEntity.ok(userMapper.toUserResponse(userRepo.save(user)));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.USER_STATUS_CHANGED, entityType = "User")
    public ResponseEntity<UserResponse> updateUserStatus(Long id, UserStatusRequest req) {
        return userRepo.findById(id).map(user -> {
            user.setStatus(req.getStatus());
            return ResponseEntity.ok(userMapper.toUserResponse(userRepo.save(user)));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.USER_DELETED, entityType = "User")
    public ResponseEntity<Void> deleteUser(Long id) {
        return userRepo.findById(id).map(user -> {
            switch (user.getRole()) {
                case DRIVER -> driverRepo.findByUser(user).ifPresent(driverRepo::delete);
                case VENDOR -> vendorRepo.findByUser(user).ifPresent(vendorRepo::delete);
                case WAREHOUSE_MANAGER -> wmRepo.findByUser(user).ifPresent(wmRepo::delete);
                case CUSTOMER-> customerRepo.findByUser(user).ifPresent(customerRepo::delete);
                case ADMIN -> { }
            }
            userRepo.delete(user);
            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    //helper fns

    private record ProfileResult(Long id, String table) {}

    private ProfileResult createRoleProfile(User user, UserRegistrationRequest req) {
        return switch (user.getRole()) {
            case CUSTOMER -> {
                Customer c = customerRepo.save(userMapper.toCustomer(user, req));
                yield new ProfileResult(c.getId(), "customers");
            }
            case VENDOR -> {
                Vendor v = vendorRepo.save(userMapper.toVendor(user, req));
                yield new ProfileResult(v.getId(), "vendors");
            }
            case WAREHOUSE_MANAGER -> {
                WarehouseManager wm = wmRepo.save(userMapper.toWarehouseManager(user, req));
                yield new ProfileResult(wm.getId(), "warehouse_managers");
            }
            case DRIVER -> {
                Driver d = driverRepo.save(userMapper.toDriver(user, req));
                yield new ProfileResult(d.getId(), "drivers");
            }
            case ADMIN -> new ProfileResult(null, null);
        };
    }

    private ProfileResult resolveRoleProfile(User user) {
        return switch (user.getRole()) {
            case CUSTOMER -> customerRepo.findByUser(user)
                    .map(c -> new ProfileResult(c.getId(), "customers"))
                    .orElse(new ProfileResult(null, null));
            case VENDOR -> vendorRepo.findByUser(user)
                    .map(v -> new ProfileResult(v.getId(), "vendors"))
                    .orElse(new ProfileResult(null, null));
            case WAREHOUSE_MANAGER -> wmRepo.findByUser(user)
                    .map(wm -> new ProfileResult(wm.getId(), "warehouse_managers"))
                    .orElse(new ProfileResult(null, null));
            case DRIVER -> driverRepo.findByUser(user)
                    .map(d -> new ProfileResult(d.getId(), "drivers"))
                    .orElse(new ProfileResult(null, null));
            case ADMIN -> new ProfileResult(null, null);
        };
    }
}