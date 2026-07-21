package com.cts.logichain360.service;

import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.dto.response.DriverResponse;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface DriverService {
    ResponseEntity<DriverResponse> getDriverById(Long id);
    ResponseEntity<DriverResponse> getDriverByUserId(Long userId);
    ResponseEntity<List<DriverResponse>> getAllDrivers();
    ResponseEntity<List<DriverResponse>> getAvailableDrivers();
    ResponseEntity<List<DriverResponse>> getAvailableDriversByLocation(String location);
    ResponseEntity<DriverResponse> updateDriver(Long id, UpdateDriverRequest request);
    ResponseEntity<DriverResponse> updateAvailability(Long id, DriverAvailabilityRequest request);
    ResponseEntity<Void> deleteDriver(Long id);
}