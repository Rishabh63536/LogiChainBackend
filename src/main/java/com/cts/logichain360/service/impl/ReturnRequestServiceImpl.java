package com.cts.logichain360.service.impl;

import com.cts.logichain360.dto.request.ApproveReturnRequest;
import com.cts.logichain360.dto.request.CreateReturnRequestRequest;
import com.cts.logichain360.dto.request.RejectReturnRequest;
import com.cts.logichain360.dto.response.ReturnRequestResponse;
import com.cts.logichain360.entity.*;
import com.cts.logichain360.enums.OrderStatus;
import com.cts.logichain360.enums.PaymentStatus;
import com.cts.logichain360.enums.PaymentType;
import com.cts.logichain360.enums.ReturnReason;
import com.cts.logichain360.enums.ReturnStatus;
import com.cts.logichain360.exception.ResourceNotFoundException;
import com.cts.logichain360.mapper.ReturnRequestMapper;
import com.cts.logichain360.repository.*;
import com.cts.logichain360.service.NotificationService;
import com.cts.logichain360.service.ReturnRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnRequestServiceImpl implements ReturnRequestService {

    private final ReturnRequestRepository returnRepo;
    private final OrderRepository orderRepo;
    private final ProductWarehouseRepository pwRepo;
    private final DriverRepository driverRepo;
    private final WarehouseManagerRepository wmRepo;
    private final PaymentRepository paymentRepo; // NEW — needed to record REFUND ledger entries
    private final NotificationService notificationService;
    private final ReturnRequestMapper returnMapper;

    // Same key InvoiceServiceImpl/PaymentServiceImpl already read — kept
    // identical so refund math never drifts from what was actually charged.
    @Value("${tax.percent:18.0}")
    private double taxPercent;

    // NEW — only applied to customer-preference returns (NOT_NEEDED/OTHER),
    // never to vendor/logistics-fault returns (DAMAGED/WRONG_ITEM).
    @Value("${return.handling.fee.percent:10.0}")
    private double handlingFeePercent;

    @Override
    @Transactional
    public ResponseEntity<ReturnRequestResponse> createReturnRequest(CreateReturnRequestRequest req) {
        log.info("Creating return request, customer={}, order={}", req.getCustomerId(), req.getOrderId());

        Orders order = orderRepo.findById(req.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order " + req.getOrderId() + " not found."));

        if (!order.getCustomer().getId().equals(req.getCustomerId())) {
            throw new IllegalArgumentException( "Order " + req.getOrderId() + " does not belong to customer " + req.getCustomerId() + ".");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException( "Cannot request a return, order " + req.getOrderId() + " is " + order.getStatus()+ ", must be DELIVERED.");
        }

        // CHANGED: was existsByOrder_Id (one return per order, ever). Now checks
        // how much of the order's quantity is STILL eligible — allows multiple
        // partial returns as long as their total never exceeds what was ordered.
        Integer alreadyClaimed = returnRepo.sumReturnedQuantityByOrderId(req.getOrderId());
        int remainingReturnable = order.getQuantity() - alreadyClaimed;
        if (req.getReturnQuantity() > remainingReturnable) {
            throw new IllegalArgumentException(
                    "Cannot return " + req.getReturnQuantity() + " units of order " + req.getOrderId()
                            + " — only " + remainingReturnable + " of the original " + order.getQuantity()
                            + " remain eligible for return.");
        }

        ReturnRequest saved = returnRepo.save(ReturnRequest.builder()
                .order(order)
                .returnQuantity(req.getReturnQuantity())
                .reason(req.getReason())
                .notes(req.getNotes())
                .status(ReturnStatus.REQUESTED)
                .requestedAt(LocalDateTime.now())
                .build());

        log.info("Return request id={} created for order={}, quantity={}",
                saved.getId(), req.getOrderId(), req.getReturnQuantity());
        return new ResponseEntity<>(returnMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @Override
    @Transactional
    public ResponseEntity<ReturnRequestResponse> approve(Long returnRequestId, ApproveReturnRequest req) {
        log.info("Approving return request={} , manager={}, driver={}",returnRequestId, req.getManagerId(), req.getDriverId());

        ReturnRequest rr = load(returnRequestId);

        if (rr.getStatus() != ReturnStatus.REQUESTED) {
            throw new IllegalArgumentException("Cannot approve, return request " + returnRequestId + " is " + rr.getStatus()+ ", must be REQUESTED.");
        }

        if (!wmRepo.existsById(req.getManagerId())) {
            throw new ResourceNotFoundException("Warehouse manager " + req.getManagerId() + " not found.");
        }

        Driver driver = driverRepo.findById(req.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver " + req.getDriverId() + " not found."));

        if (driver.getAvailable() == null || !driver.getAvailable()) {
            throw new IllegalArgumentException("Driver " + driver.getId() + " is not available.");
        }

        rr.setStatus(ReturnStatus.APPROVED);
        rr.setResolvedAt(LocalDateTime.now());
        rr.setResolvedByManagerId(req.getManagerId());
        rr.setPickupDriverId(driver.getId());

        driver.setAvailable(false);
        driverRepo.save(driver);

        ReturnRequest saved = returnRepo.save(rr);
        log.info("Return request id={} approved. Driver id={} assigned for pickup.", saved.getId(), driver.getId());
        return ResponseEntity.ok(returnMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ResponseEntity<ReturnRequestResponse> reject(Long returnRequestId, RejectReturnRequest req) {
        log.info("Rejecting return request={} — manager={}", returnRequestId, req.getManagerId());

        ReturnRequest rr = load(returnRequestId);

        if (rr.getStatus() != ReturnStatus.REQUESTED) {
            throw new IllegalArgumentException("Cannot reject, return request " + returnRequestId + " is " + rr.getStatus()+ ", must be REQUESTED.");
        }

        if (!wmRepo.existsById(req.getManagerId())) {
            throw new ResourceNotFoundException("Warehouse manager " + req.getManagerId() + " not found.");
        }

        rr.setStatus(ReturnStatus.REJECTED);
        rr.setResolvedAt(LocalDateTime.now());
        rr.setResolvedByManagerId(req.getManagerId());
        if (req.getNotes() != null) {
            rr.setNotes(rr.getNotes() == null ? req.getNotes() : rr.getNotes() + " | Rejection: " + req.getNotes());
        }

        ReturnRequest saved = returnRepo.save(rr);
        log.info("Return request id={} rejected.", saved.getId());
        return ResponseEntity.ok(returnMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ResponseEntity<ReturnRequestResponse> completeRestock(Long returnRequestId, String photoFilename) {
        log.info("Completing restock for return request={}", returnRequestId);

        ReturnRequest rr = load(returnRequestId);

        if (rr.getStatus() != ReturnStatus.APPROVED) {
            throw new IllegalArgumentException("Cannot restock, return request " + returnRequestId + " is " + rr.getStatus()+ ", must be APPROVED.");
        }

        Orders order = rr.getOrder();
        ProductWarehouse pw = order.getProductWarehouse();

        // ── Refund calculation ──────────────────────────────────────────
        // Proportional to what's actually being returned, not the whole order.
        // Delivery fee is deliberately excluded — it paid for a service (the
        // delivery itself) that already happened, regardless of why the item
        // is coming back.
        double unitPrice = order.getUnitPriceSnapshot();
        double proportionalAmount = unitPrice * rr.getReturnQuantity();
        double proportionalTax = proportionalAmount * (taxPercent / 100.0);
        double grossRefund = proportionalAmount + proportionalTax;

        // Handling fee only for customer-preference returns — not the
        // customer's fault if the item was damaged or wrong, so no fee there.
        boolean vendorOrLogisticsFault = rr.getReason() == ReturnReason.DAMAGED || rr.getReason() == ReturnReason.WRONG_ITEM;
        double handlingFee = vendorOrLogisticsFault ? 0.0 : round2(grossRefund * (handlingFeePercent / 100.0));
        double netRefund = round2(grossRefund - handlingFee);

        paymentRepo.save(Payment.builder()
                .order(order)
                .amount(-netRefund) // negative — see PaymentType.REFUND for why
                .type(PaymentType.REFUND)
                .status(PaymentStatus.SUCCESS)
                .paidAt(LocalDateTime.now())
                .build());

        order.setAmountPaid(round2(order.getAmountPaid() - netRefund));

        rr.setRefundAmount(netRefund);
        rr.setHandlingFeeAmount(handlingFee);

        log.info("Refund of {} processed for return={} (order={}, qty={}, handlingFee={}, reason={}).",
                netRefund, returnRequestId, order.getId(), rr.getReturnQuantity(), handlingFee, rr.getReason());

        // ── Order status: only flip to RETURNED once EVERYTHING has come back ──
        // sumReturnedQuantityByOrderId already counts this request (its status
        // isn't REJECTED), so this correctly reflects the running total across
        // however many partial returns have happened so far.
        Integer totalReturnedSoFar = returnRepo.sumReturnedQuantityByOrderId(order.getId());
        if (totalReturnedSoFar >= order.getQuantity()) {
            order.setStatus(OrderStatus.RETURNED);
        }
        // else: leave status as DELIVERED — a partial return doesn't erase the
        // fact that the customer still holds (and paid for) the rest of it.
        Orders savedOrder = orderRepo.save(order);

        Driver driver = driverRepo.findById(rr.getPickupDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver " + rr.getPickupDriverId() + " not found"));
        driver.setAvailable(true);
        driverRepo.save(driver);

        rr.setStatus(ReturnStatus.RESTOCKED);
        rr.setRestockedAt(LocalDateTime.now());
        if (photoFilename != null) {
            rr.setPhotoFilename(photoFilename);
        }
        ReturnRequest saved = returnRepo.save(rr);

        // Only fire the order-status notification if the order's status
        // actually changed — a partial return leaves it DELIVERED, and there's
        // no informative "order status changed to DELIVERED" message worth
        // sending. (A dedicated "partial return processed" notification would
        // be a good future addition — not built here, flagging it as a gap.)
        if (savedOrder.getStatus() == OrderStatus.RETURNED) {
            try {
                notificationService.notifyOrderStatusChanged(savedOrder);
            } catch (Exception e) {
                log.error("Failed to send ORDER_STATUS_CHANGED (RETURNED) notification for order={}: {}",
                        savedOrder.getId(), e.getMessage(), e);
            }
        }

        return ResponseEntity.ok(returnMapper.toResponse(saved));
    }

    /** Avoids floating point artifacts like 4999.499999999999. */
    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @Override
    public ResponseEntity<ReturnRequestResponse> getById(Long returnRequestId) {
        return ResponseEntity.ok(returnMapper.toResponse(load(returnRequestId)));
    }

    @Override
    public ResponseEntity<List<ReturnRequestResponse>> getByCustomerId(Long customerId) {
        return ResponseEntity.ok(returnRepo.findAllByOrder_Customer_IdOrderByRequestedAtDesc(customerId).stream().map(returnMapper::toResponse).toList());
    }

    @Override
    public ResponseEntity<List<ReturnRequestResponse>> getPending() {
        return ResponseEntity.ok(returnRepo.findAllByStatusOrderByRequestedAtAsc(ReturnStatus.REQUESTED).stream().map(returnMapper::toResponse).toList());
    }

    @Override
    public ResponseEntity<List<ReturnRequestResponse>> getByDriverId(Long driverId) {
        if (!driverRepo.existsById(driverId)) {
            throw new ResourceNotFoundException("Driver " + driverId + " not found.");
        }
        return ResponseEntity.ok(returnRepo.findAllByPickupDriverIdAndStatusOrderByResolvedAtAsc(driverId, ReturnStatus.APPROVED).stream().map(returnMapper::toResponse).toList());
    }

    private ReturnRequest load(Long id) {
        return returnRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return request " + id + " not found."));
    }
}