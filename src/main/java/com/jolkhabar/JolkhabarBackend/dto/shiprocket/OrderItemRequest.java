package com.jolkhabar.JolkhabarBackend.dto.shiprocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemRequest {
    private String name;
    private String sku;
    private int units;
    @JsonProperty("selling_price")
    private double sellingPrice;
}

