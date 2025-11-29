package com.jolkhabar.JolkhabarBackend.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "razorpay")
public class RazorpayProperties {

    @NotBlank
    private String keyId;

    @NotBlank
    private String keySecret;
}
