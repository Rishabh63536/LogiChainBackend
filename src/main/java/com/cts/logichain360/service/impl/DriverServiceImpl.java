package com.cts.logichain360.service.impl;

import com.cts.logichain360.annotation.Auditable;
import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.enums.AuditAction;
import com.cts.logichain360.dto.response.DriverResponse;
import com.cts.logichain360.mapper.DriverMapper;
import com.cts.logichain360.repository.DriverRepository;
import com.cts.logichain360.repository.WarehouseRepository;
import com.cts.logichain360.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {
    private final DriverRepository driverRepo;
    private final DriverMapper driverMapper;
    private WarehouseRepository warehouseRepo;

    @Override
    public ResponseEntity<DriverResponse> getDriverById(Long id) {
        return driverRepo.findById(id)
                .map(d -> ResponseEntity.ok(driverMapper.toResponse(d)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<DriverResponse> getDriverByUserId(Long userId) {
        return driverRepo.findByUser_Id(userId)
                .map(d -> ResponseEntity.ok(driverMapper.toResponse(d)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {
        return ResponseEntity.ok(driverRepo.findAll().stream().map(driverMapper::toResponse).toList());
    }

    @Override
    public ResponseEntity<List<DriverResponse>> getAvailableDrivers() {
        return ResponseEntity.ok(driverRepo.findAllByAvailableTrue().stream().map(driverMapper::toResponse).toList());
    }

    @Override
    public ResponseEntity<List<DriverResponse>> getAvailableDriversByLocation(String location) {
        return ResponseEntity.ok(driverRepo.findAllByAvailableTrueAndLocation(location).stream().map(driverMapper::toResponse).toList());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DRIVER_UPDATED, entityType = "Driver")
    public ResponseEntity<DriverResponse> updateDriver(Long id, UpdateDriverRequest req) {
        if(req.getLocation() != null && !warehouseRepo.existsByLocationIgnoreCase(req.getLocation())){
            throw new IllegalArgumentException(("City '" + req.getLocation() +"' does not match any existing warehouse location"));
        }
        return driverRepo.findById(id)
                .map(d -> {
                    if (req.getLicenseNumber() != null) d.setLicenseNumber(req.getLicenseNumber());
                    if (req.getLicenseExpiry() != null) d.setLicenseExpiry(req.getLicenseExpiry());
                    if(req.getLocation() != null) d.setLocation((req.getLocation()));
                    return ResponseEntity.ok(driverMapper.toResponse(driverRepo.save(d)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DRIVER_AVAILABILITY_CHANGED, entityType = "Driver")
    public ResponseEntity<DriverResponse> updateAvailability(Long id, DriverAvailabilityRequest req) {
        return driverRepo.findById(id)
                .map(d -> {
                    d.setAvailable(req.getAvailable());
                    return ResponseEntity.ok(driverMapper.toResponse(driverRepo.save(d)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DRIVER_DELETED, entityType = "Driver")
    public ResponseEntity<Void> deleteDriver(Long id) {
        return driverRepo.findById(id)
                .map(d -> {
                    driverRepo.delete(d);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}