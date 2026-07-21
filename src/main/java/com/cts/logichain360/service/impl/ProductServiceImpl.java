package com.cts.logichain360.service.impl;

import com.cts.logichain360.annotation.Auditable;
import com.cts.logichain360.dto.request.*;
import com.cts.logichain360.enums.AuditAction;
import com.cts.logichain360.enums.UserRole;
import com.cts.logichain360.dto.response.ProductResponse;
import com.cts.logichain360.entity.Product;
import com.cts.logichain360.entity.User;
import com.cts.logichain360.entity.Vendor;
import com.cts.logichain360.exception.ResourceNotFoundException;
import com.cts.logichain360.exception.UserAlreadyExistsException;
import com.cts.logichain360.mapper.ProductMapper;
import com.cts.logichain360.repository.ProductRepository;
import com.cts.logichain360.repository.UserRepository;
import com.cts.logichain360.repository.VendorRepository;
import com.cts.logichain360.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final VendorRepository vendorRepo;
    private final UserRepository userRepo; // NEW — needed to resolve "who is actually calling this"
    private final ProductMapper productMapper;

    @Override
    @Transactional
    @Auditable(action = AuditAction.PRODUCT_CREATED, entityType = "Product")
    public ResponseEntity<ProductResponse> createProduct(CreateProductRequest req) {
        log.info("Creating product '{}' for vendor {}", req.getProductName(), req.getVendorId());

        Vendor vendor = vendorRepo.findById(req.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor " + req.getVendorId() + " not found."));

        if (productRepo.existsByProductNameAndVendor_Id(req.getProductName(), vendor.getId())) {
            log.warn("Duplicate product name '{}' for vendor {}", req.getProductName(), vendor.getId());
            //TODO: will create and chnage the exception later
            throw new UserAlreadyExistsException(
                    "Vendor " + vendor.getId() + " already has a product named '"
                            + req.getProductName() + "'.");
        }

        Product saved = productRepo.save(Product.builder()
                .productName(req.getProductName())
                .productPrice(req.getProductPrice())
                .productDescription(req.getProductDescription())
                .vendor(vendor)
                .build());

        log.info("Created product id={} for vendor {}", saved.getProductId(), vendor.getId());
        return new ResponseEntity<>(productMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ProductResponse> getProductById(Long id) {
        log.debug("Fetching product id={}", id);
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + id + " not found."));
        return ResponseEntity.ok(productMapper.toResponse(p));
    }

    @Override
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.debug("Fetching all products");
        return ResponseEntity.ok(productRepo.findAll().stream().map(productMapper::toResponse).toList());
    }

    @Override
    public ResponseEntity<List<ProductResponse>> getProductsByVendor(Long vendorId) {
        log.debug("Fetching all products for vendor={}", vendorId);
        if (!vendorRepo.existsById(vendorId)) {
            throw new ResourceNotFoundException("Vendor " + vendorId + " not found.");
        }
        return ResponseEntity.ok(productRepo.findAllByVendor_Id(vendorId)
                .stream().map(productMapper::toResponse).toList());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.PRODUCT_UPDATED, entityType = "Product")
    public ResponseEntity<ProductResponse> updateProduct(Long id, UpdateProductRequest req) {
        log.info("Updating product id={}", id);
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + id + " not found."));

        verifyProductOwnership(p); // SECURITY FIX: previously any VENDOR could edit any product

        if (req.getProductName() != null && !req.getProductName().equals(p.getProductName())) {
            if (productRepo.existsByProductNameAndVendor_Id(
                    req.getProductName(), p.getVendor().getId())) {
                throw new UserAlreadyExistsException(
                        "Vendor " + p.getVendor().getId() + " already has a product named '"
                                + req.getProductName() + "'.");
            }
            p.setProductName(req.getProductName());
        }
        if (req.getProductPrice()       != null) p.setProductPrice(req.getProductPrice());
        if (req.getProductDescription() != null) p.setProductDescription(req.getProductDescription());

        return ResponseEntity.ok(productMapper.toResponse(productRepo.save(p)));
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.PRODUCT_DELETED, entityType = "Product")
    public ResponseEntity<Void> deleteProduct(Long id) {
        log.info("Deleting product id={}", id);
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + id + " not found."));

        verifyProductOwnership(p); // SECURITY FIX: previously any VENDOR could delete any product

        productRepo.delete(p);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Confirms the caller is either ADMIN, or the Vendor who actually owns this
     * product. Fixes a real gap: update/delete previously only checked role
     * (ADMIN/VENDOR), never WHICH vendor — any vendor could edit/delete any
     * other vendor's product by guessing product ids.
     */
    private void verifyProductOwnership(Product product) {
        String phone = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByPhoneAndIsDeletedFalse(phone)
                .orElseThrow(() -> new AccessDeniedException("Unable to verify identity."));

        if (currentUser.getRole() == UserRole.ADMIN) {
            return; // admins can manage any product
        }

        Vendor callingVendor = vendorRepo.findByUser(currentUser)
                .orElseThrow(() -> new AccessDeniedException("No vendor profile for this account."));

        if (!callingVendor.getId().equals(product.getVendor().getId())) {
            throw new AccessDeniedException("You can only modify your own products.");
        }
    }
}