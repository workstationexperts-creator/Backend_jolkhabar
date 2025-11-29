package com.jolkhabar.JolkhabarBackend.controller;

// import com.jolkhabar.JolkhabarBackend.dto.shiprocket.ShipmentResult;
import com.jolkhabar.JolkhabarBackend.service.ShiprocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
// import java.util.Map;

@RestController
@RequestMapping("/api/v1/shiprocket")
@RequiredArgsConstructor
@Slf4j
public class ShiprocketController {

    private final ShiprocketService shiprocketService;

    @GetMapping("/shipments")
    public ResponseEntity<?> getShipments() {
        log.info("🟡 Shiprocket is currently disabled for this build.");
        return ResponseEntity.ok(List.of()); // return an empty list
    }

    // you can leave other endpoints commented for now
}
