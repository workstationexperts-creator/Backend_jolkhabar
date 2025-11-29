package com.jolkhabar.JolkhabarBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {

    private Integer id;
    private LocalDateTime orderDate;
    private String status;
    private double totalPrice;

    private AddressDto shippingAddress;
    private List<OrderItemDto> items;

    // ======================
    // 🚚 Shiprocket Integration
    // ======================
    private String shiprocketOrderId;
    private String shiprocketShipmentId;
    private String shiprocketAwb;
    private String shiprocketTrackingUrl;

    // ======================
    // 💳 Razorpay Integration (future)
    // ======================
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}
