package com.jolkhabar.JolkhabarBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private String imageUrl;
    private Integer categoryId;
    
    @Builder.Default
    private boolean active = true;
}
