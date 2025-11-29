package com.jolkhabar.JolkhabarBackend.controller;

import com.jolkhabar.JolkhabarBackend.model.Order;
import com.jolkhabar.JolkhabarBackend.service.OrderService;
import com.jolkhabar.JolkhabarBackend.service.RazorpayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final RazorpayService razorpayService;
    private final OrderService orderService;

    // ============================================================
    // ✅ STEP 1: Create Razorpay Order
    // ============================================================
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> payload) {
        try {
            if (!payload.containsKey("orderId")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing required field: orderId"));
            }

            Integer orderId = Integer.parseInt(payload.get("orderId").toString());
            var localOrderOpt = orderService.findById(orderId);
            if (localOrderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Order not found for ID: " + orderId));
            }

            Order localOrder = localOrderOpt.get();

            if (!"PENDING".equalsIgnoreCase(localOrder.getStatus())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Only PENDING orders can be processed for payment."));
            }

            String receiptId = "ORDER_" + localOrder.getId();
            var razorpayOrder = razorpayService.createOrder(localOrder.getTotalPrice(), receiptId);

            localOrder.setRazorpayOrderId(razorpayOrder.get("id").toString());
            orderService.save(localOrder);

            log.info("✅ Razorpay order created | Local ID: {} | Razorpay ID: {}",
                    localOrder.getId(), razorpayOrder.get("id"));

            return ResponseEntity.ok(Map.of(
                    "key", razorpayService.getKeyId(),
                    "amount", razorpayOrder.get("amount"),
                    "currency", razorpayOrder.get("currency"),
                    "orderId", razorpayOrder.get("id"),
                    "receipt", razorpayOrder.get("receipt"),
                    "localOrderId", localOrder.getId()
            ));
        } catch (Exception e) {
            log.error("❌ Failed to create Razorpay order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create Razorpay order. Please try again."));
        }
    }

    // ============================================================
    // ✅ STEP 2: Verify Payment (NO Shiprocket)
    // ============================================================
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> payload) {
        try {
            String razorpayOrderId = payload.get("razorpay_order_id");
            String razorpayPaymentId = payload.get("razorpay_payment_id");
            String razorpaySignature = payload.get("razorpay_signature");

            boolean verified = razorpayService.verifySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature);

            if (!verified) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "failed",
                        "message", "Invalid Razorpay payment signature."
                ));
            }

            // ✅ Update order status to PAID
            var localOrder = orderService.findByRazorpayOrderId(razorpayOrderId)
                    .orElseThrow(() -> new RuntimeException("Order not found for Razorpay ID: " + razorpayOrderId));

            localOrder.setRazorpayPaymentId(razorpayPaymentId);
            localOrder.setRazorpaySignature(razorpaySignature);
            localOrder.setStatus("PAID");
            orderService.save(localOrder);

            log.info("💳 Payment verified for Order #{} | Payment ID: {}", localOrder.getId(), razorpayPaymentId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Payment verified successfully.",
                    "orderId", localOrder.getId(),
                    "orderStatus", localOrder.getStatus()
            ));
        } catch (Exception e) {
            log.error("❌ Payment verification failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    // ============================================================
    // ✅ STEP 3: (Optional) Admin view all orders
    // ============================================================
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders() {
        var allOrders = orderService.findAll();
        return ResponseEntity.ok(allOrders);
    }
}
