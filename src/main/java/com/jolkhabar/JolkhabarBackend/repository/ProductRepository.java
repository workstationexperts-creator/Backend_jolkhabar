package com.jolkhabar.JolkhabarBackend.repository;

import com.jolkhabar.JolkhabarBackend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // These methods will be used for the public storefront to show only active products
    List<Product> findAllByCategoryIdAndActiveTrue(Integer categoryId);
    List<Product> findAllByActiveTrue();

    // This method will be used by the admin panel to see all products (active and inactive)
    List<Product> findAllByCategoryId(Integer categoryId);
}

