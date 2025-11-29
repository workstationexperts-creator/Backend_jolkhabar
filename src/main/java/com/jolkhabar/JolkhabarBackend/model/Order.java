package com.jolkhabar.JolkhabarBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ✅ Ensure Address is imported explicitly
import com.jolkhabar.JolkhabarBackend.model.Address;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    // ✅ Embedded shipping address stored inline in the "orders" table
    @Embedded
    private Address shippingAddress;

    private LocalDateTime orderDate;

    private double totalPrice;

    /**
     * Order status lifecycle:
     * PENDING → PAID → SHIPPED → DELIVERED / CANCELLED
     */
    private String status;

    // ======================
    // 🚚 Shiprocket Integration
    // ======================
    @Column(name = "shiprocket_order_id")
    private String shiprocketOrderId;

    @Column(name = "shiprocket_shipment_id")
    private String shiprocketShipmentId;

    @Column(name = "shiprocket_awb")
    private String shiprocketAwb;

    @Column(name = "shiprocket_tracking_url", columnDefinition = "TEXT")
    private String shiprocketTrackingUrl;

    // ======================
    // 💳 Razorpay Integration
    // ======================
    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature")
    private String razorpaySignature;

    @Column(name = "order_number", unique = true)
    private String orderNumber;

    // ======================
    // 🔧 Utility setters
    // ======================
    public void setPaymentId(String paymentId) {
        this.razorpayPaymentId = paymentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shiprocketShipmentId = shipmentId;
    }

    public void setTrackingUrl(String trackingUrl) {
        this.shiprocketTrackingUrl = trackingUrl;
    }
}
