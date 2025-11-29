package com.jolkhabar.JolkhabarBackend.controller;

import com.jolkhabar.JolkhabarBackend.dto.AddressDto;
import com.jolkhabar.JolkhabarBackend.dto.OrderDto;
import com.jolkhabar.JolkhabarBackend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ✅ Place an order (before payment)
    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestBody AddressDto addressDto) {
        try {
            String razorpayOrderId = addressDto.getRazorpayOrderId();
            OrderDto newOrder = orderService.placeOrder(addressDto, razorpayOrderId);
            return ResponseEntity.ok(newOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Admin - Get all orders
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // ✅ Admin - Update order status manually
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Integer orderId,
            @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    // ✅ Customer - Track order by number
    @GetMapping("/track/{orderNumber}")
    public ResponseEntity<?> trackOrder(@PathVariable String orderNumber) {
        var optionalOrder = orderService.findByOrderNumber(orderNumber);
        if (optionalOrder.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No order found with number: " + orderNumber));
        }

        var order = optionalOrder.get();
        if (order.getShiprocketTrackingUrl() != null) {
            return ResponseEntity.ok(Map.of(
                    "orderNumber", order.getOrderNumber(),
                    "status", order.getStatus(),
                    "trackingUrl", order.getShiprocketTrackingUrl(),
                    "awb", order.getShiprocketAwb(),
                    "message", "You can track your shipment using the provided URL."));
        } else {
            return ResponseEntity.ok(Map.of(
                    "orderNumber", order.getOrderNumber(),
                    "status", order.getStatus(),
                    "message", "Shipment not yet created. Please check again later."));
        }
    }

    // Customer - Get logged-in user's orders
    @GetMapping("/my")
    public ResponseEntity<List<OrderDto>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrdersAsDto());
    }

}
