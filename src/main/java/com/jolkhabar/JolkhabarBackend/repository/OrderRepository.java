package com.jolkhabar.JolkhabarBackend.repository;

import com.jolkhabar.JolkhabarBackend.model.Order;
import com.jolkhabar.JolkhabarBackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByUserOrderByOrderDateDesc(User user);
}
