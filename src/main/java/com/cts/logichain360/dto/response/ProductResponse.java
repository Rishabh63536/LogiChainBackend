package com.cts.logichain360.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Vendor's catalog product. Use /product-warehouses to see stock at each warehouse.")
public class ProductResponse {

    @Schema(example = "1")
    private Long productId;
    @Schema(example = "Sony WH-1000XM5 Headphones")
    private String productName;
    @Schema(example = "29990.00")
    private Double productPrice;
    @Schema(example = "Wireless headphones")
    private String productDescription;
    @Schema(example = "1")
    private Long vendorId;
    @Schema(example = "Sony India Pvt Ltd")
    private String vendorCompanyName;
}