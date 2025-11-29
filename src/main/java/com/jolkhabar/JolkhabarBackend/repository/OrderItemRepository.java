package com.jolkhabar.JolkhabarBackend.repository;

import com.jolkhabar.JolkhabarBackend.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    boolean existsByProductId(Long productId);
}
