package com.jolkhabar.JolkhabarBackend.controller;

import com.jolkhabar.JolkhabarBackend.dto.CartDto;
import com.jolkhabar.JolkhabarBackend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<CartDto> addToCart(@RequestParam Long productId, @RequestParam int quantity) { // <-- Changed to Long
        return ResponseEntity.ok(cartService.addToCart(productId, quantity));
    }

    @GetMapping
    public ResponseEntity<CartDto> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @DeleteMapping("/remove")
    public ResponseEntity<CartDto> removeItemFromCart(@RequestParam Long productId) { // <-- Changed to Long
        return ResponseEntity.ok(cartService.removeItemFromCart(productId));
    }
    
    @PutMapping("/update")
    public ResponseEntity<CartDto> updateItemQuantity(@RequestParam Long productId, @RequestParam int quantity) { // <-- Changed to Long
        return ResponseEntity.ok(cartService.updateItemQuantity(productId, quantity));
    }
}