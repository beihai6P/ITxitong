package com.company.itoms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WxLoginRequest {
    @NotBlank(message = "Code cannot be blank")
    private String code;
    
    // Optional: if the user needs to bind with an existing username/password
    private String username;
    private String password;
}
