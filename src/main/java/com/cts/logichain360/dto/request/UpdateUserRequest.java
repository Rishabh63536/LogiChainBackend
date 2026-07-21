
// --- dto/request/UpdateUserRequest.java ---
package com.cts.logichain360.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {
    private String name;
    private String phone;
    private String password;
}