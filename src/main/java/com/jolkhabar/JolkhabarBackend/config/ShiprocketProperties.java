package com.jolkhabar.JolkhabarBackend.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "shiprocket")
public class ShiprocketProperties {

    /**
     * Your Shiprocket login email.
     */
    @NotBlank
    private String email;

    /**
     * Your Shiprocket account password.
     */
    @NotBlank
    private String password;

    /**
     * Base URL for Shiprocket API.
     * Default: https://apiv2.shiprocket.in/v1/external
     */
    @NotBlank
    private String baseUrl = "https://apiv2.shiprocket.in/v1/external";
}
