package com.jolkhabar.JolkhabarBackend.service;

import com.jolkhabar.JolkhabarBackend.dto.CartDto;
import com.jolkhabar.JolkhabarBackend.dto.CartItemDto;
import com.jolkhabar.JolkhabarBackend.model.*;
import com.jolkhabar.JolkhabarBackend.repository.CartRepository;
import com.jolkhabar.JolkhabarBackend.repository.ProductRepository;
import com.jolkhabar.JolkhabarBackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    // The unused cartItemRepository has been removed.

    @Transactional
    public CartDto addToCart(Long productId, int quantity) {
        User user = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Cart cart = getCartForUser(user);

        // Check if item is already in cart
        cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + quantity),
                        () -> {
                            CartItem newItem = new CartItem();
                            newItem.setProduct(product);
                            newItem.setQuantity(quantity);
                            newItem.setCart(cart);
                            cart.getItems().add(newItem);
                        });

        updateCartTotalPrice(cart);
        Cart savedCart = cartRepository.save(cart);
        return mapToDto(savedCart);
    }

    @Transactional
    public CartDto removeItemFromCart(Long productId) {
        User user = getCurrentUser();
        Cart cart = getCartForUser(user);

        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        // By removing the item from the list, orphanRemoval=true will handle the delete
        cart.getItems().remove(itemToRemove);

        updateCartTotalPrice(cart);
        Cart savedCart = cartRepository.save(cart);
        return mapToDto(savedCart);
    }

    @Transactional
    public CartDto updateItemQuantity(Long productId, int quantity) {
        User user = getCurrentUser();
        Cart cart = getCartForUser(user);

        CartItem itemToUpdate = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        if (quantity <= 0) {
            // If quantity is zero or less, remove the item directly from the list.
            // orphanRemoval=true will handle the database deletion.
            cart.getItems().remove(itemToUpdate);
        } else {
            // Otherwise, just update the quantity.
            itemToUpdate.setQuantity(quantity);
        }

        updateCartTotalPrice(cart);
        Cart savedCart = cartRepository.save(cart);
        return mapToDto(savedCart);
    }

    public CartDto getCart() {
        User user = getCurrentUser();
        Cart cart = getCartForUser(user);
        return mapToDto(cart);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Cart getCartForUser(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    private void updateCartTotalPrice(Cart cart) {
        double totalPrice = cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        cart.setTotalPrice(totalPrice);
    }

    private CartDto mapToDto(Cart cart) {
        return CartDto.builder()
                .id(cart.getId())
                .items(cart.getItems().stream().map(this::mapToCartItemDto).collect(Collectors.toList()))
                .totalPrice(cart.getTotalPrice())
                .build();
    }

    private CartItemDto mapToCartItemDto(CartItem cartItem) {
        return CartItemDto.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProduct().getId())
                .productName(cartItem.getProduct().getName())
                .price(cartItem.getProduct().getPrice())
                .quantity(cartItem.getQuantity())
                .imageUrl(cartItem.getProduct().getImageUrl())
                .build();
    }

    public void clearCart(Cart cart) {
        cart.getItems().clear(); // orphanRemoval=true will delete items
        cart.setTotalPrice(0.0);
        cartRepository.save(cart);
    }

}
