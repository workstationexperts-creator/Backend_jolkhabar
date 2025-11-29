package com.jolkhabar.JolkhabarBackend.repository;

import com.jolkhabar.JolkhabarBackend.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
}
