package com.jolkhabar.JolkhabarBackend.repository;

import com.jolkhabar.JolkhabarBackend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // <-- This is the fix

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // This new method will be used by the public site to show only active categories
    List<Category> findAllByActiveTrue();
}

