package com.jolkhabar.JolkhabarBackend.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex; // ✅ ADD THIS IMPORT
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for all Razorpay operations:
 * - Create orders
 * - Verify signatures
 * - Return safe structured responses for frontend
 */
@Service
@RequiredArgsConstructor
public class RazorpayService {

    private static final Logger logger = LoggerFactory.getLogger(RazorpayService.class);

    // ✅ API credentials
    @Getter
    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    private RazorpayClient client;

    private synchronized RazorpayClient getClient() throws Exception {
        if (client == null) {
            client = new RazorpayClient(keyId, keySecret);
            logger.info("🔑 Razorpay client initialized successfully with key ID: {}", keyId);
        }
        return client;
    }

    /**
     * ✅ Creates a Razorpay Order linked with your local order.
     */
    public Map<String, Object> createOrder(double amountInRupees, String receiptId) {
        try {
            RazorpayClient client = getClient();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int) (amountInRupees * 100)); // Convert INR → paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", receiptId);
            orderRequest.put("payment_capture", true);

            Order order = client.orders.create(orderRequest);

            logger.info("✅ Razorpay Order Created | Razorpay ID: {} | Amount: {} INR | Receipt: {}",
                    order.get("id"), amountInRupees, receiptId);

            Map<String, Object> response = new HashMap<>();
            response.put("id", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("receipt", order.get("receipt"));
            response.put("key", keyId);

            return response;

        } catch (Exception e) {
            logger.error("❌ Failed to create Razorpay order: {}", e.getMessage(), e);
            throw new RuntimeException("Unable to create Razorpay order. Please try again.", e);
        }
    }

    /**
     * ✅ FIXED: Verifies Razorpay payment signature using HEX encoding
     */
    public boolean verifySignature(String orderId, String paymentId, String razorpaySignature) {
    try {
        if (orderId == null || paymentId == null || razorpaySignature == null) {
            logger.warn("⚠️ Missing parameters for Razorpay signature verification.");
            return false;
        }

        String payload = orderId + "|" + paymentId;

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(keySecret.getBytes(), "HmacSHA256");
        mac.init(secretKeySpec);

        byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        // Convert bytes to HEX (Razorpay sends signature in HEX format)
        StringBuilder hash = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hash.append('0');
            hash.append(hex);
        }

        String generatedSignature = hash.toString();

        boolean isValid = generatedSignature.equals(razorpaySignature);
        if (isValid) {
            logger.info("✅ Razorpay signature verified successfully for order {}", orderId);
        } else {
            logger.error("❌ Signature mismatch.\nExpected: {}\nReceived: {}", generatedSignature, razorpaySignature);
        }

        return isValid;

    } catch (Exception e) {
        logger.error("🚨 Error verifying Razorpay signature: {}", e.getMessage(), e);
        return false;
    }
}


    public int convertRupeesToPaise(double amountInRupees) {
        return (int) (amountInRupees * 100);
    }
}
