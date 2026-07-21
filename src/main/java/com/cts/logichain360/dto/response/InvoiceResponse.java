package com.cts.logichain360.dto.response;

import com.cts.logichain360.enums.InvoiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Invoice generated on order confirmation.")
public class InvoiceResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "INV-2026-00001")
    private String invoiceNumber;

    private Long orderId;
    private Long customerId;
    private String customerName;
    private String customerCompany;

    private Long vendorId;
    private String vendorCompanyName;

    private String productName;
    private Integer quantity;
    private Double unitPrice;
    private Double subtotal;

    @Schema(example = "18.0", description = "GST percentage applied.")
    private Double taxPercent;
    private Double taxAmount;

    private Double deliveryFee;
    private Double totalAmount;

    private String shippingAddress;

    @Schema(example = "2026-06-01T10:30:00")
    private LocalDateTime issuedAt;

    private InvoiceStatus status;

    @Schema(description = "Populated only when status = VOID.")
    private LocalDateTime voidedAt;
}
