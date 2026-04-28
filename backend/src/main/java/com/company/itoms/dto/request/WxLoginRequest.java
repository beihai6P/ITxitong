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

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
