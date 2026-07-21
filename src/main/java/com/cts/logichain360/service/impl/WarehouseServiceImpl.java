package com.cts.logichain360.service.impl;

import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.dto.response.WarehouseResponse;
import com.cts.logichain360.entity.Warehouse;
import com.cts.logichain360.entity.WarehouseManager;
import com.cts.logichain360.exception.ResourceNotFoundException;
import com.cts.logichain360.exception.UserAlreadyExistsException;
import com.cts.logichain360.mapper.WarehouseMapper;
import com.cts.logichain360.repository.ProductWarehouseRepository;
import com.cts.logichain360.repository.WarehouseManagerRepository;
import com.cts.logichain360.repository.WarehouseRepository;
import com.cts.logichain360.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepo;
    private final WarehouseManagerRepository wmRepo;
    private final ProductWarehouseRepository pwRepo;
    private final WarehouseMapper warehouseMapper;

    @Override
    @Transactional
    public ResponseEntity<WarehouseResponse> createWarehouse(CreateWarehouseRequest req) {
        if (warehouseRepo.existsByWarehouseCode(req.getWarehouseCode())) {
            throw new UserAlreadyExistsException(
                    "Warehouse code '" + req.getWarehouseCode() + "' already in use.");
        }
        Warehouse saved = warehouseRepo.save(Warehouse.builder()
                .warehouseCode(req.getWarehouseCode())
                .location(req.getLocation())
                .capacity(req.getCapacity())
                .build());
        return new ResponseEntity<>(toResponse(saved), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<WarehouseResponse> getById(Long id) {
        Warehouse w = warehouseRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse " + id + " not found."));
        return ResponseEntity.ok(toResponse(w));
    }

    @Override
    public ResponseEntity<List<WarehouseResponse>> getAll() {
        return ResponseEntity.ok(warehouseRepo.findAll().stream().map(this::toResponse).toList());
    }

    @Override
    @Transactional
    public ResponseEntity<WarehouseResponse> update(Long id, UpdateWarehouseRequest req) {
        Warehouse w = warehouseRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse " + id + " not found."));

        if (req.getWarehouseCode() != null && !req.getWarehouseCode().equals(w.getWarehouseCode())) {
            if (warehouseRepo.existsByWarehouseCode(req.getWarehouseCode())) {
                throw new UserAlreadyExistsException(
                        "Warehouse code '" + req.getWarehouseCode() + "' already in use.");
            }
            w.setWarehouseCode(req.getWarehouseCode());
        }
        if (req.getLocation() != null) w.setLocation(req.getLocation());
        if (req.getCapacity() != null){
            Integer alreadyAllocated = pwRepo.sumMaxStockByWarehouseId(w.getId());
            if(req.getCapacity() < alreadyAllocated){
                throw new IllegalArgumentException("Cannot reduce capacity to "+ req.getCapacity()+" "+ alreadyAllocated+" units are already allocated across products at this warehouse");
            }
            w.setCapacity(req.getCapacity());
        }

        return ResponseEntity.ok(toResponse(warehouseRepo.save(w)));
    }

    @Override
    @Transactional
    public ResponseEntity<Void> delete(Long id) {
        Warehouse w = warehouseRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse " + id + " not found."));

        // If a manager points at this warehouse, clear that link first so the
        // FK constraint doesn't block the hard delete.
        wmRepo.findByAssignedWarehouse_Id(id).ifPresent(m -> {
            m.setAssignedWarehouse(null);
            wmRepo.save(m);
        });

        warehouseRepo.delete(w);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Looks up the assigned manager (other side of the relationship) via the derived
    // query, then delegates the flattening into the DTO to the mapper.
    private WarehouseResponse toResponse(Warehouse w) {
        WarehouseManager manager = wmRepo.findByAssignedWarehouse_Id(w.getId()).orElse(null);
        return warehouseMapper.toResponse(w, manager);
    }
}