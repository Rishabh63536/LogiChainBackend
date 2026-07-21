package com.cts.logichain360.dto.response;

import com.cts.logichain360.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A customer's order with the product, driver (if assigned), and current status.")
public class OrderResponse {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "CONFIRMED")
    private OrderStatus status;
    @Schema(example = "2026-06-01T10:30:00")
    private LocalDateTime placedAt;
    @Schema(example = "20")
    private Integer quantity;
    @Schema(example = "599800.00")
    private Double totalAmount;
    @Schema(example = "12 MG Road, Bangalore 560001")
    private String shippingAddress;

    private Double amountPaid;

    // Customer
    private Long customerId;
    private String customerName;
    private String customerPhone;

    // Product (snapshots)
    private Long productId;
    @Schema(description = "Product name as it was at order time (survives later product changes/deletion).")
    private String productNameSnapshot;
    @Schema(description = "Unit price as it was at order time.")
    private Double unitPriceSnapshot;

    // Vendor(for vendor facing views)
    private Long vendorId;
    private String vendorCompanyName;

    // Warehouse(which stock entry was decremented)
    private Long warehouseId;
    private String warehouseCode;
    private Long productWarehouseId;

    // Driver(null until assigned)
    private Long driverId;
    private String driverName;
}