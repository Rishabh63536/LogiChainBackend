package com.cts.logichain360.service.impl;

import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.dto.response.ProductWarehouseResponse;
import com.cts.logichain360.entity.*;
import com.cts.logichain360.exception.ResourceNotFoundException;
import com.cts.logichain360.exception.UserAlreadyExistsException;
import com.cts.logichain360.mapper.ProductWarehouseMapper;
import com.cts.logichain360.repository.*;
import com.cts.logichain360.service.ProductWarehouseService;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
//@AllArgsConstructor
@Slf4j
public class ProductWarehouseServiceImpl implements ProductWarehouseService {

    private final ProductWarehouseRepository pwRepo;
    private final ProductRepository productRepo;
    private final WarehouseRepository warehouseRepo;
    private final ProductWarehouseMapper productWarehouseMapper;

    @Override
    @Transactional
    public ResponseEntity<ProductWarehouseResponse> launch(LaunchProductAtWarehouseRequest req) {
        log.info("Launching product {} at warehouse {} with initial stock {}",
                req.getProductId(), req.getWarehouseId(), req.getStock());

        Product product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product " + req.getProductId() + " not found."));

        Warehouse warehouse = warehouseRepo.findById(req.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Warehouse " + req.getWarehouseId() + " not found."));

        // 1:1 today: refuse if this product was already launched somewhere.
        if (pwRepo.existsByProduct_ProductId(product.getProductId())) {
            log.warn("Refused launch — product {} already launched at another warehouse.",
                    product.getProductId());
            throw new UserAlreadyExistsException(
                    "Product " + product.getProductId() + " is already launched at a warehouse.");
        }

        if (req.getStock() > req.getMaxStock()) {
            throw new IllegalArgumentException(
                    "Initial stock (" + req.getStock() + ") cannot exceed maxStock (" + req.getMaxStock() + ").");
        }

        // NEW — capacity accounting: this product's maxStock must fit in whatever
        // room is left at the warehouse, after everything already launched there.
        Integer alreadyAllocated = pwRepo.sumMaxStockByWarehouseId(warehouse.getId());
        int remainingCapacity = warehouse.getCapacity() - alreadyAllocated;
        if (req.getMaxStock() > remainingCapacity) {
            throw new IllegalArgumentException(
                    "maxStock (" + req.getMaxStock() + ") exceeds remaining warehouse capacity. "
                            + "Warehouse capacity=" + warehouse.getCapacity()
                            + ", already allocated to other products=" + alreadyAllocated
                            + ", remaining=" + remainingCapacity + ".");
        }

        ProductWarehouse saved = pwRepo.save(ProductWarehouse.builder()
                .product(product)
                .warehouse(warehouse)
                .stock(req.getStock())
                .maxStock(req.getMaxStock())
                .rolPercent(req.getRolPercent())
                .build());

        log.info("Launched product-warehouse id={} (product={}, warehouse={})",
                saved.getId(), product.getProductId(), warehouse.getId());

        return new ResponseEntity<>(productWarehouseMapper.toResponse(saved), HttpStatus.CREATED);
    }

    //fetching based on product-warehouse id
    @Override
    public ResponseEntity<ProductWarehouseResponse> getById(Long id) {
        log.debug("Fetching product-warehouse id={}", id);
        ProductWarehouse pw = pwRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product-warehouse entry " + id + " not found."));
        return ResponseEntity.ok(productWarehouseMapper.toResponse(pw));
    }

    @Override
    public ResponseEntity<ProductWarehouseResponse> getByProductId(Long productId) {
        log.debug("Fetching product-warehouse by productId={}", productId);
        ProductWarehouse pw = pwRepo.findByProduct_ProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product " + productId + " is not launched at any warehouse."));
        return ResponseEntity.ok(productWarehouseMapper.toResponse(pw));
    }

    @Override
    public ResponseEntity<List<ProductWarehouseResponse>> getByWarehouseId(Long warehouseId) {
        log.debug("Fetching all product-warehouse entries for warehouse={}", warehouseId);
        if (!warehouseRepo.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse " + warehouseId + " not found.");
        }
        return ResponseEntity.ok(pwRepo.findAllByWarehouse_Id(warehouseId)
                .stream().map(obj->productWarehouseMapper.toResponse(obj)).toList());
    }

    @Override
    public ResponseEntity<List<ProductWarehouseResponse>> getAll() {
        return ResponseEntity.ok(pwRepo.findAll().stream().map(obj->productWarehouseMapper.toResponse(obj)).toList());
    }

    @Override
    public ResponseEntity<List<ProductWarehouseResponse>> getLowStock() {
        log.info("Querying global low-stock entries (below ROL).");
        List<ProductWarehouse> below = pwRepo.findAllBelowRol();
        if (!below.isEmpty()) {
            log.warn("{} product-warehouse entries are below their reorder level.", below.size());
        }
        return ResponseEntity.ok(below.stream().map(obj->productWarehouseMapper.toResponse(obj)).toList());
    }

    @Override
    public ResponseEntity<List<ProductWarehouseResponse>> getLowStockByWarehouse(Long warehouseId) {
        log.info("Querying low-stock entries at warehouse={}", warehouseId);
        if (!warehouseRepo.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse " + warehouseId + " not found.");
        }
        List<ProductWarehouse> below = pwRepo.findAllBelowRolByWarehouse(warehouseId);
        if (!below.isEmpty()) {
            log.warn("{} entries are below ROL at warehouse {}.", below.size(), warehouseId);
        }
        return ResponseEntity.ok(below.stream().map(obj->productWarehouseMapper.toResponse(obj)).toList());
    }

    @Override
    @Transactional
    public ResponseEntity<ProductWarehouseResponse> updateThresholds(Long id, UpdateProductWarehouseRequest req) {
        log.info("Updating thresholds for product-warehouse id={}", id);
        ProductWarehouse pw = pwRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product-warehouse entry " + id + " not found."));

        // NEW — capacity accounting, checked BEFORE mutating anything, using the
        // exclude-self query so this row's OWN old maxStock isn't counted against
        // itself (that would make every warehouse look artificially "full").
        if (req.getMaxStock() != null) {
            Integer allocatedExcludingSelf =
                    pwRepo.sumMaxStockByWarehouseIdExcluding(pw.getWarehouse().getId(), pw.getId());
            int remainingCapacity = pw.getWarehouse().getCapacity() - allocatedExcludingSelf;
            if (req.getMaxStock() > remainingCapacity) {
                throw new IllegalArgumentException(
                        "New maxStock (" + req.getMaxStock() + ") exceeds remaining warehouse capacity. "
                                + "Warehouse capacity=" + pw.getWarehouse().getCapacity()
                                + ", allocated to other products=" + allocatedExcludingSelf
                                + ", remaining=" + remainingCapacity + ".");
            }
        }

        if (req.getMaxStock()   != null) pw.setMaxStock(req.getMaxStock());
        if (req.getRolPercent() != null) pw.setRolPercent(req.getRolPercent());

        if (pw.getStock() > pw.getMaxStock()) {
            throw new IllegalArgumentException(
                    "Current stock (" + pw.getStock() + ") exceeds new maxStock (" + pw.getMaxStock() + ").");
        }

        return ResponseEntity.ok(productWarehouseMapper.toResponse(pwRepo.save(pw)));
    }

    @Override
    @Transactional
    public ResponseEntity<ProductWarehouseResponse> restock(Long id, RestockRequest req) {
        log.info("Restocking product-warehouse id={} by {} units", id, req.getAmount());
        ProductWarehouse pw = pwRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product-warehouse entry " + id + " not found."));

        int newStock = pw.getStock() + req.getAmount();
        if (newStock > pw.getMaxStock()) {
            throw new IllegalArgumentException(
                    "Restock would exceed maxStock. current=" + pw.getStock()
                            + ", add=" + req.getAmount() + ", max=" + pw.getMaxStock());
        }
        pw.setStock(newStock);
        ProductWarehouse saved = pwRepo.save(pw);
        log.info("Restocked id={} to stock={} (max={})", saved.getId(), saved.getStock(), saved.getMaxStock());
        return ResponseEntity.ok(productWarehouseMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ResponseEntity<Void> delete(Long id) {
        log.info("Deleting product-warehouse id={}", id);
        ProductWarehouse pw = pwRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product-warehouse entry " + id + " not found."));
        pwRepo.delete(pw);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}