package com.cts.logichain360.dto.response;

import com.cts.logichain360.enums.UserRole;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private Long userId;
    private String name;
    private String phone;
    private UserRole role;
    private Long roleProfileId;
    private String roleProfileTable;
    private Long warehouseId;
}