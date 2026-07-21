package com.cts.logichain360.dto.response;

import com.cts.logichain360.enums.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationResponse {
    private Long userId;
    private String name;
    private String phone;
    private UserRole role;
    private UserStatus status;
    private Long roleProfileId;
    private String roleProfileTable;

}