package com.jolkhabar.JolkhabarBackend.dto.shiprocket;

import lombok.Data;

@Data
public class ShipmentResponse {
    private Long orderId;
    private Long shipmentId;
    private String awb;
    private String trackingUrl;
}
