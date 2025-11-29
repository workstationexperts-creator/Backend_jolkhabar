package com.jolkhabar.JolkhabarBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    private double price; // Always in INR

    private int stock;

    @Column(length = 1024)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // ✅ Keep default 'true' when using Lombok @Builder
    @Builder.Default
    private boolean active = true;

    // ✅ Optional: Display helper (for frontend formatting)
    public String getFormattedPrice() {
        return String.format("₹%.2f", price);
    }
}
