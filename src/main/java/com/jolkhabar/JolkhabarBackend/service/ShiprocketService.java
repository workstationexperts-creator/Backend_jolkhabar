package com.jolkhabar.JolkhabarBackend.service;

import com.jolkhabar.JolkhabarBackend.dto.shiprocket.ShipmentResult;
import com.jolkhabar.JolkhabarBackend.model.Address;
import com.jolkhabar.JolkhabarBackend.model.Order;
import com.jolkhabar.JolkhabarBackend.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class ShiprocketService {

    private final RestTemplate restTemplate;

    @Value("${shiprocket.base-url:https://apiv2.shiprocket.in/v1/external}")
    private String baseUrl;

    @Value("${shiprocket.email:}")
    private String email;

    @Value("${shiprocket.password:}")
    private String password;

    // Thread-safe shared auth info
    private volatile String authToken;
    private final AtomicLong tokenExpiryTime = new AtomicLong(0);

    public ShiprocketService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ===============================================================
    // ✅ AUTH: Automatically login to Shiprocket if token missing or expired
    // ===============================================================
    private synchronized void ensureAuthenticated() {
        long now = System.currentTimeMillis();

        if (authToken != null && now < tokenExpiryTime.get()) {
            return; // Token still valid
        }

        try {
            String url = baseUrl + "/auth/login";
            Map<String, String> payload = Map.of(
                    "email", email,
                    "password", password
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object tokenObj = response.getBody().get("token");
                if (tokenObj != null) {
                    authToken = tokenObj.toString();
                    tokenExpiryTime.set(now + (23 * 60 * 60 * 1000)); // Token valid 23h
                    log.info("🔐 Shiprocket token refreshed successfully.");
                } else {
                    log.warn("⚠️ Shiprocket login succeeded but token missing in response.");
                }
            } else {
                log.error("⚠️ Shiprocket login failed: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ Shiprocket authentication failed: {}", e.getMessage(), e);
        }
    }

    // ===============================================================
    // 🔁 TOKEN REFRESH SCHEDULER (every 22 hours)
    // ===============================================================
    @Scheduled(fixedDelay = 22 * 60 * 60 * 1000) // 22 hours
    public void scheduledTokenRefresh() {
        log.info("⏰ Scheduled Shiprocket token refresh triggered...");
        ensureAuthenticated();
    }

    // ===============================================================
    // ✅ ADMIN: Fetch all Shiprocket shipments (sync dashboard)
    // ===============================================================
    public List<ShipmentResult> getAllShipments() {
        try {
            ensureAuthenticated();

            String url = baseUrl + "/shipments";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            ResponseEntity<ShipmentResult[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), ShipmentResult[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("✅ Retrieved {} shipments from Shiprocket", response.getBody().length);
                return List.of(response.getBody());
            }

            log.warn("⚠️ Shiprocket returned {} with empty body", response.getStatusCode());
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("❌ Error fetching shipments from Shiprocket: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // ===============================================================
    // ✅ Create a shipment after successful payment
    // ===============================================================
    public ShipmentResult createShipment(Order order) {
        try {
            ensureAuthenticated();

            if (authToken == null || authToken.isBlank()) {
                log.warn("⚠️ Shiprocket token missing — using mock shipment.");
                return mockShipment(order);
            }

            String url = baseUrl + "/orders/create/adhoc";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            User user = order.getUser();
            Address addr = order.getShippingAddress();

            String name = Optional.ofNullable(addr)
                    .map(Address::getRecipientName)
                    .orElse(user != null ? user.getFirstname() + " " + user.getLastname() : "Customer");

            String emailAddr = user != null ? user.getEmail() : "customer@example.com";
            String phone = Optional.ofNullable(addr).map(Address::getPhoneNumber).orElse("9999999999");

            String addressLine = Optional.ofNullable(addr).map(Address::getStreet).orElse("Unknown Street");
            String city = Optional.ofNullable(addr).map(Address::getCity).orElse("Unknown City");
            String state = Optional.ofNullable(addr).map(Address::getState).orElse("Unknown State");
            String country = Optional.ofNullable(addr).map(Address::getCountry).orElse("India");
            String pincode = Optional.ofNullable(addr).map(Address::getPostalCode).orElse("000000");

            String orderDate = Optional.ofNullable(order.getOrderDate())
                    .orElse(LocalDateTime.now())
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            double total = order.getTotalPrice();

            Map<String, Object> payload = Map.ofEntries(
                    Map.entry("order_id", "ORDER-" + order.getId()),
                    Map.entry("order_date", orderDate),
                    Map.entry("pickup_location", "Primary Warehouse"),
                    Map.entry("billing_customer_name", name),
                    Map.entry("billing_last_name", ""),
                    Map.entry("billing_address", addressLine),
                    Map.entry("billing_city", city),
                    Map.entry("billing_pincode", pincode),
                    Map.entry("billing_state", state),
                    Map.entry("billing_country", country),
                    Map.entry("billing_email", emailAddr),
                    Map.entry("billing_phone", phone),
                    Map.entry("shipping_is_billing", true),
                    Map.entry("order_items", List.of(Map.of(
                            "name", "Jolkhabar Order #" + order.getId(),
                            "sku", "SKU-" + order.getId(),
                            "units", 1,
                            "selling_price", total
                    ))),
                    Map.entry("payment_method", "Prepaid"),
                    Map.entry("sub_total", total),
                    Map.entry("length", 10),
                    Map.entry("breadth", 10),
                    Map.entry("height", 10),
                    Map.entry("weight", 0.5)
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {}
            );

            Map<String, Object> res = response.getBody();

            if (response.getStatusCode().is2xxSuccessful() && res != null) {
                ShipmentResult shipment = new ShipmentResult();
                shipment.setOrderId(String.valueOf(order.getId()));
                shipment.setShipmentId(String.valueOf(res.getOrDefault("shipment_id", "SR-" + System.currentTimeMillis())));
                shipment.setAwb(String.valueOf(res.getOrDefault("awb_code", "AWB-" + System.nanoTime())));
                shipment.setTrackingUrl(String.valueOf(res.getOrDefault(
                        "tracking_url", "https://shiprocket.in/track/" + order.getId()
                )));
                shipment.setStatus("CREATED");

                log.info("✅ Shiprocket order created: {}", shipment);
                return shipment;
            }

            log.warn("⚠️ Shiprocket returned non-success: {}", response.getStatusCode());
            return mockShipment(order);

        } catch (Exception e) {
            log.error("❌ Shiprocket shipment creation failed: {}", e.getMessage(), e);
            return mockShipment(order);
        }
    }

    // ===============================================================
    // 🧪 Mock shipment fallback (safe dev mode)
    // ===============================================================
    private ShipmentResult mockShipment(Order order) {
        ShipmentResult mock = new ShipmentResult();
        mock.setOrderId(String.valueOf(order.getId()));
        mock.setShipmentId("SR-" + System.currentTimeMillis());
        mock.setAwb("AWB-" + System.nanoTime());
        mock.setTrackingUrl("https://mock.shiprocket.in/track/" + order.getId());
        mock.setStatus("TEST_MODE");
        return mock;
    }
}
