
package com.cts.logichain360.dto.request;

import com.cts.logichain360.enums.UserStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusRequest {
    private UserStatus status;
}