package com.jolkhabar.JolkhabarBackend.repository;

import com.jolkhabar.JolkhabarBackend.model.Cart;
import com.jolkhabar.JolkhabarBackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    // This method allows us to find a cart by the user who owns it.
    Optional<Cart> findByUser(User user);
}
