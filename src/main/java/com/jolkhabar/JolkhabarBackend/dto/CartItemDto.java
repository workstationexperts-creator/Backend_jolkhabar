// File: com/jolkhabar/JolkhabarBackend/dto/CartItemDto.java
package com.jolkhabar.JolkhabarBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDto {
    private Integer id;
    private Long productId;
    private String productName;
    private int quantity;
    private double price;
    private String imageUrl; 
}