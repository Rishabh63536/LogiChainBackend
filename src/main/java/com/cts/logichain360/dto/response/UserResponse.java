package com.cts.logichain360.dto.response;

import com.cts.logichain360.enums.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String phone;
    private UserRole role;
    private UserStatus status;
}