package com.jolkhabar.JolkhabarBackend.dto.shiprocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResult {
    private String orderId;
    private String shipmentId;
    private String awb;
    private String trackingUrl;
    private String status;
}
