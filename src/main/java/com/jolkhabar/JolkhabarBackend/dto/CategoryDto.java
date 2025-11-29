package com.jolkhabar.JolkhabarBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {

    private Integer id;
    private String name;
    private String description;
    private String imageUrl;
    private String bannerImageUrl;
    private String layoutType;

    
    @Builder.Default
    private boolean active = true;
}
