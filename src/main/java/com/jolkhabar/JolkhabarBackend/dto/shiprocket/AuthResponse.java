package com.jolkhabar.JolkhabarBackend.dto.shiprocket;

import lombok.Data;

@Data
public class AuthResponse {
    // Shiprocket sends other fields, but we only need the token
    private String token;
}

