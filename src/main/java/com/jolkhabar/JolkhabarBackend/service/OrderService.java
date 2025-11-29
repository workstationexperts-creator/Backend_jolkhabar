package com.jolkhabar.JolkhabarBackend.service;

import com.jolkhabar.JolkhabarBackend.dto.AddressDto;
import com.jolkhabar.JolkhabarBackend.dto.OrderDto;
import com.jolkhabar.JolkhabarBackend.dto.OrderItemDto;
import com.jolkhabar.JolkhabarBackend.dto.shiprocket.ShipmentResult;
import com.jolkhabar.JolkhabarBackend.model.*;
import com.jolkhabar.JolkhabarBackend.repository.CartRepository;
import com.jolkhabar.JolkhabarBackend.repository.OrderRepository;
import com.jolkhabar.JolkhabarBackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jolkhabar.JolkhabarBackend.model.User; 


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ShiprocketService shiprocketService;

    // ===============================================================
    // ✅ STEP 1: Place an Order (creates a pending order)
    // ===============================================================
    @Transactional
    public OrderDto placeOrder(AddressDto addressDto, String razorpayOrderId) {
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot place an order with an empty cart.");
        }

        // Build and save order
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalPrice(cart.getTotalPrice());
        order.setRazorpayOrderId(razorpayOrderId);

        Address shippingAddress = Address.builder()
                .recipientName(addressDto.getRecipientName())
                .phoneNumber(addressDto.getPhoneNumber())
                .street(addressDto.getStreet())
                .city(addressDto.getCity())
                .state(addressDto.getState())
                .postalCode(addressDto.getPostalCode())
                .country(addressDto.getCountry())
                .build();
        order.setShippingAddress(shippingAddress);

        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
            OrderItem item = new OrderItem();
            item.setProduct(cartItem.getProduct());
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(cartItem.getProduct().getPrice());
            item.setOrder(order);
            return item;
        }).collect(Collectors.toList());

        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Clear the user's cart
        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("🛒 Order placed successfully for user {} with total ₹{}", user.getEmail(), order.getTotalPrice());
        return mapToOrderDto(savedOrder);
    }

    // ===============================================================
    // ✅ STEP 2: Handle Payment Verification & Shiprocket Shipment
    // ===============================================================
    @Transactional
    public Order handleSuccessfulPayment(String razorpayOrderId, String paymentId, String signature) {
        Order order = orderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found for Razorpay ID: " + razorpayOrderId));

        order.setRazorpayPaymentId(paymentId);
        order.setRazorpaySignature(signature);
        order.setStatus("PAID");
        orderRepository.save(order);

        log.info("💳 Payment verified for order {} | Payment ID: {}", order.getId(), paymentId);

        try {
            ShipmentResult shipment = shiprocketService.createShipment(order);
            if (shipment != null && shipment.getShipmentId() != null) {
                order.setShiprocketOrderId(shipment.getOrderId());
                order.setShiprocketShipmentId(shipment.getShipmentId());
                order.setShiprocketAwb(shipment.getAwb());
                order.setShiprocketTrackingUrl(shipment.getTrackingUrl());
                order.setStatus("SHIPPED");
                orderRepository.save(order);

                log.info("🚚 Shiprocket shipment created successfully for order {} | Tracking: {}",
                        order.getId(), shipment.getTrackingUrl());
            } else {
                log.warn("⚠️ Shiprocket did not return shipment details for order {}", order.getId());
            }
        } catch (Exception e) {
            log.error("🚨 Failed to create Shiprocket shipment for order {}: {}", order.getId(), e.getMessage());
        }

        return order;
    }

    // ===============================================================
    // ✅ ADMIN FEATURES
    // ===============================================================

    /** Fetch all orders (for admin dashboard) */
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToOrderDto)
                .collect(Collectors.toList());
    }

    /** Update order status manually (admin override) */
    @Transactional
    public OrderDto updateOrderStatus(Integer orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        return mapToOrderDto(orderRepository.save(order));
    }

    // ===============================================================
    // ✅ SUPPORT METHODS
    // ===============================================================

    public Optional<Order> findByRazorpayOrderId(String razorpayOrderId) {
        return orderRepository.findByRazorpayOrderId(razorpayOrderId);
    }

    public Optional<Order> findById(Integer id) {
        return orderRepository.findById(id);
    }

    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    // ===============================================================
    // ✅ INTERNAL UTILITIES
    // ===============================================================

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private OrderDto mapToOrderDto(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .shippingAddress(mapToAddressDto(order.getShippingAddress()))
                .items(order.getItems().stream()
                        .map(this::mapToOrderItemDto)
                        .collect(Collectors.toList()))
                .shiprocketOrderId(order.getShiprocketOrderId())
                .shiprocketShipmentId(order.getShiprocketShipmentId())
                .shiprocketAwb(order.getShiprocketAwb())
                .shiprocketTrackingUrl(order.getShiprocketTrackingUrl())
                .razorpayOrderId(order.getRazorpayOrderId())
                .razorpayPaymentId(order.getRazorpayPaymentId())
                .razorpaySignature(order.getRazorpaySignature())
                .build();
    }

    private AddressDto mapToAddressDto(Address address) {
        if (address == null)
            return null;
        return AddressDto.builder()
                .recipientName(address.getRecipientName())
                .phoneNumber(address.getPhoneNumber())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .build();
    }

    private OrderItemDto mapToOrderItemDto(OrderItem item) {
        return OrderItemDto.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build();
    }

    public List<OrderDto> getMyOrdersAsDto() {
        User user = getCurrentUser();
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);
        return orders.stream()
                .map(this::mapToOrderDto)
                .toList();
    }

}
