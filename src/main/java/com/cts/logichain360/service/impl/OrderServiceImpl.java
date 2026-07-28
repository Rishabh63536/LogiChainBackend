package com.cts.logichain360.service.impl;

import com.cts.logichain360.annotation.Auditable;
import com.cts.logichain360.dto.request.AssignDriverRequest;
import com.cts.logichain360.dto.request.PlaceOrderRequest;
import com.cts.logichain360.dto.response.OrderResponse;
import com.cts.logichain360.entity.*;
import com.cts.logichain360.enums.AuditAction;
import com.cts.logichain360.enums.OrderStatus;
import com.cts.logichain360.enums.PaymentStatus;
import com.cts.logichain360.enums.PaymentType;
import com.cts.logichain360.exception.InsufficientStockException;
import com.cts.logichain360.exception.ResourceNotFoundException;
import com.cts.logichain360.mapper.OrderMapper;
import com.cts.logichain360.repository.*;
import com.cts.logichain360.service.InvoiceService;
import com.cts.logichain360.service.NotificationService;
import com.cts.logichain360.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final Set<OrderStatus> ACTIVE_STATUSES = Set.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.ASSIGNED, OrderStatus.IN_TRANSIT);
    private static final Set<OrderStatus> PAST_STATUSES = Set.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED, OrderStatus.RETURNED);
    private static final Set<OrderStatus> CANCELLABLE_STATUSES = Set.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.ASSIGNED);

    private final OrderRepository orderRepo;
    private final CustomerRepository customerRepo;
    private final DriverRepository driverRepo;
    private final ProductWarehouseRepository pwRepo;
    private final PODRepository podRepo;
    private final PaymentRepository paymentRepo;
    private final NotificationService notificationService;
    private final InvoiceService invoiceService;
    private final OrderMapper orderMapper;
    private final WarehouseRepository warehouseRepo;

    //place order
    @Override
    @Transactional
    @Auditable(action = AuditAction.ORDER_PLACED, entityType = "Order")
    public ResponseEntity<OrderResponse> placeOrder(PlaceOrderRequest req) {
        log.info("Placing order: customer={}, product={}, qty={}", req.getCustomerId(), req.getProductId(), req.getQuantity());

        Customer customer = customerRepo.findById(req.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer " + req.getCustomerId() + " not found"));

        ProductWarehouse pw = pwRepo.findByProduct_ProductId(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product " + req.getProductId() + " is not launched at any warehouse"));

        if (pw.getStock() < req.getQuantity()) {
            log.warn("Insufficient stock — product={}, requested={}, available={}",req.getProductId(), req.getQuantity(), pw.getStock());
            throw new InsufficientStockException("Insufficient stock for product " + req.getProductId()+ ". Requested " + req.getQuantity()+ ", available " + pw.getStock() + ".");
        }

        //updating stock of wh after purchase
        int newStock = pw.getStock() - req.getQuantity();
        pw.setStock(newStock);

        Product product = pw.getProduct();
        double unitPrice= product.getProductPrice();
        double total = unitPrice * req.getQuantity();

        Orders order = Orders.builder()
                .customer(customer)
                .product(product)
                .productWarehouse(pw)
                .quantity(req.getQuantity())
                .productNameSnapshot(product.getProductName())
                .unitPriceSnapshot(unitPrice)
                .totalAmount(total)
                .amountPaid(0.0)
                .status(OrderStatus.PENDING)
                .placedAt(LocalDateTime.now())
                .shippingAddress(req.getShippingAddress())
                .build();

        Orders saved = orderRepo.save(order);
        pwRepo.save(pw);

        log.info("Order id={} placed as PENDING (awaiting advance payment). Stock at pw={} reduced to {}.",saved.getId(), pw.getId(), newStock);

        if (pw.isBelowRol()) {
            try {
                notificationService.notifyRolBreach(pw);
            } catch (Exception e) {
                log.error("Failed to send ROL notification for pw={}: {}", pw.getId(), e.getMessage(), e);
            }
        }

        return new ResponseEntity<>(orderMapper.toResponse(saved), HttpStatus.CREATED);
    }


    @Override
    public ResponseEntity<OrderResponse> getOrderById(Long orderId) {
        log.debug("Fetching order id={}", orderId);
        return ResponseEntity.ok(orderMapper.toResponse(load(orderId)));
    }

    @Override
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(Long customerId) {
        if (!customerRepo.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer " + customerId + " not found");
        }
        return ResponseEntity.ok(orderRepo.findAllByCustomer_IdOrderByPlacedAtDesc(customerId).stream().map(orderMapper::toResponse).toList());
    }

    @Override
    public ResponseEntity<List<OrderResponse>> getActiveOrdersByCustomer(Long customerId) {
        if (!customerRepo.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer " + customerId + " not found");
        }
        return ResponseEntity.ok(orderRepo.findAllByCustomer_IdAndStatusInOrderByPlacedAtDesc(customerId, ACTIVE_STATUSES).stream().map(orderMapper::toResponse).toList());
    }

    @Override
    public ResponseEntity<List<OrderResponse>> getPastOrdersByCustomer(Long customerId) {
        if (!customerRepo.existsById(customerId))
            throw new ResourceNotFoundException("Customer " + customerId + " not found.");
        return ResponseEntity.ok(orderRepo
                .findAllByCustomer_IdAndStatusInOrderByPlacedAtDesc(customerId, PAST_STATUSES)
                .stream().map(orderMapper::toResponse).toList());
    }

    @Override
    public ResponseEntity<List<OrderResponse>> getOrdersByDriver(Long driverId) {
        if (!driverRepo.existsById(driverId)) {
            throw new ResourceNotFoundException("Driver " + driverId + " not found.");
        }
        return ResponseEntity.ok(orderRepo.findAllByDriver_IdOrderByPlacedAtDesc(driverId).stream().map(orderMapper::toResponse).toList());
    }

    @Override
    public ResponseEntity<List<OrderResponse>> getOrdersByWarehouse(Long warehouseId) {
        if (!warehouseRepo.existsById(warehouseId))
            throw new ResourceNotFoundException("Warehouse " + warehouseId + " not found.");
        return ResponseEntity.ok(orderRepo.findAllByProductWarehouse_Warehouse_IdOrderByPlacedAtDesc(warehouseId)
                .stream().map(orderMapper::toResponse).toList());
    }

    @Override
    public ResponseEntity<List<OrderResponse>> getOrdersAwaitingAssignment(Long warehouseId) {
        if (!warehouseRepo.existsById(warehouseId))
            throw new ResourceNotFoundException("Warehouse " + warehouseId + " not found.");
        return ResponseEntity.ok(orderRepo
                .findAllByProductWarehouse_Warehouse_IdAndStatusOrderByPlacedAtAsc(warehouseId, OrderStatus.CONFIRMED)
                .stream().map(orderMapper::toResponse).toList());
    }

    @Override
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderRepo
                .findAll()
                .stream().map(orderMapper:: toResponse).toList());
    }

    //state transitions
    @Override
    @Transactional
    @Auditable(action = AuditAction.ORDER_DRIVER_ASSIGNED, entityType = "Order")
    public ResponseEntity<OrderResponse> assignDriver(Long orderId, AssignDriverRequest req) {
        log.info("Assigning driver={} to order={}", req.getDriverId(), orderId);
        Orders order = load(orderId);

        if (order.getStatus() != OrderStatus.CONFIRMED)
            throw new IllegalArgumentException("Cannot assign driver — order " + orderId + " is " + order.getStatus() + ", must be CONFIRMED.");

        Driver driver = driverRepo.findById(req.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver " + req.getDriverId() + " not found."));

        if (driver.getAvailable() == null || !driver.getAvailable()) {
            throw new IllegalArgumentException("Driver " + driver.getId() + " is not available.");
        }

        order.setDriver(driver);
        order.setStatus(OrderStatus.ASSIGNED);
        driver.setAvailable(false);
        driverRepo.save(driver);
        Orders saved = orderRepo.save(order);
        log.info("Order id={} assigned to driver id={}", saved.getId(), driver.getId());

        //notifying driver
        try {
            notificationService.notifyDriverAssigned(saved);
        } catch (Exception e) {
            log.error("Failed to send ORDER_ASSIGNED notification for order={}: {}", orderId, e.getMessage(), e);
        }
        try {
            notificationService.notifyOrderStatusChanged(saved);
        } catch (Exception e) {
            log.error("Failed to send ORDER_STATUS_CHANGED notification for order={}: {}", orderId, e.getMessage(), e);
        }

        return ResponseEntity.ok(orderMapper.toResponse(saved));
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.ORDER_DELIVERY_STARTED, entityType = "Order")
    public ResponseEntity<OrderResponse> startDelivery(Long orderId) {
        log.info("Starting delivery for order={}", orderId);
        Orders order = load(orderId);
        if (order.getStatus() != OrderStatus.ASSIGNED) {
            throw new IllegalArgumentException("Cannot start delivery — order " + orderId + " is " + order.getStatus() + ", must be ASSIGNED.");
        }
        order.setStatus(OrderStatus.IN_TRANSIT);
        Orders saved = orderRepo.save(order);

        //notify customer
        try {
            notificationService.notifyOrderStatusChanged(saved);
        } catch (Exception e) {
            log.error("Failed to send ORDER_STATUS_CHANGED notification for order={}: {}", orderId, e.getMessage(), e);
        }

        return ResponseEntity.ok(orderMapper.toResponse(saved));
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.ORDER_DELIVERED, entityType = "Order")
    public ResponseEntity<OrderResponse> completeDelivery(Long orderId, String photoFilename) {
        log.info("Completing delivery for order={} with POD photo={}", orderId, photoFilename);
        Orders order = load(orderId);

        if (order.getStatus() != OrderStatus.IN_TRANSIT) {
            throw new IllegalArgumentException("Cannot complete delivery — order " + orderId + " is " + order.getStatus() + ", must be IN_TRANSIT.");
        }

        if (!order.isFullyPaid()) {
            throw new IllegalArgumentException("Cannot complete delivery — final payment not yet received for order " + orderId + ".");
        }

        Driver driver = order.getDriver();
        if (driver == null) {
            throw new IllegalArgumentException("Cannot complete delivery — order " + orderId + " has no assigned driver.");
        }

        podRepo.save(POD.builder()
                .order(order)
                .photoFilename(photoFilename)
                .driverId(driver.getId())
                .driverName(driver.getUser().getName())
                .uploadedAt(LocalDateTime.now())
                .build());

        order.setStatus(OrderStatus.DELIVERED);
        driver.setAvailable(true);
        driverRepo.save(driver);
        log.info("Driver id={} freed after delivery. POD recorded for order={}.", driver.getId(), orderId);

        Orders saved = orderRepo.save(order);

        try {
            notificationService.notifyOrderStatusChanged(saved);
        } catch (Exception e) {
            log.error("Failed to send ORDER_STATUS_CHANGED notification for order={}: {}", orderId, e.getMessage(), e);
        }

        return ResponseEntity.ok(orderMapper.toResponse(saved));
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.ORDER_CANCELLED, entityType = "Order")
    public ResponseEntity<OrderResponse> cancelOrder(Long orderId) {
        log.info("Cancelling order={}", orderId);
        Orders order = load(orderId);

        if (!CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new IllegalArgumentException("Cannot cancel — order " + orderId + " is " + order.getStatus()+ ". Cancellation allowed only before IN_TRANSIT.");
        }

        ProductWarehouse pw = order.getProductWarehouse();
        int restored = pw.getStock() + order.getQuantity();
        if (restored > pw.getMaxStock()) {
            log.warn("Restored stock {} exceeds maxStock {} on pw={}. Clamping.", restored, pw.getMaxStock(), pw.getId());
            restored = pw.getMaxStock();
        }
        pw.setStock(restored);
        pwRepo.save(pw);

        Driver driver = order.getDriver();
        if (driver != null) {
            driver.setAvailable(true);
            driverRepo.save(driver);
            log.info("Driver id={} freed by cancellation.", driver.getId());
        }

        if (order.getAmountPaid() > 0) {
            double refundAmount = order.getAmountPaid();

            paymentRepo.save(Payment.builder()
                    .order(order)
                    .amount(-refundAmount)
                    .type(PaymentType.REFUND)
                    .status(PaymentStatus.SUCCESS)
                    .paidAt(LocalDateTime.now())
                    .build());

            log.info("Refunded {} to customer for cancelled order={} (full advance, no handling fee).",
                    refundAmount, orderId);
            order.setAmountPaid(0.0);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Orders saved = orderRepo.save(order);
        log.info("Order id={} cancelled. Stock at pw={} restored to {}.", saved.getId(), pw.getId(), restored);

        try {
            invoiceService.voidInvoice(orderId);
        } catch (Exception e) {
            log.error("Invoice void failed for orderId={}: {}", orderId, e.getMessage(), e);
        }

        try {
            notificationService.notifyOrderStatusChanged(saved);
        } catch (Exception e) {
            log.error("Failed to send ORDER_STATUS_CHANGED notification for order={}: {}", orderId, e.getMessage(), e);
        }

        return ResponseEntity.ok(orderMapper.toResponse(saved));
    }

    // helper fn

    private Orders load(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + id + " not found."));
    }
}