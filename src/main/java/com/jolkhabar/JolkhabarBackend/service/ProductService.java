package com.jolkhabar.JolkhabarBackend.service;

import com.jolkhabar.JolkhabarBackend.dto.ProductDto;
import com.jolkhabar.JolkhabarBackend.model.Category;
import com.jolkhabar.JolkhabarBackend.model.Product;
import com.jolkhabar.JolkhabarBackend.repository.CategoryRepository;
import com.jolkhabar.JolkhabarBackend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    // We removed OrderItemRepository as the check is no longer needed for soft delete

    public ProductDto addProduct(ProductDto productDto) {
        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setStock(productDto.getStock());
        product.setImageUrl(productDto.getImageUrl());
        product.setCategory(category);
        product.setActive(true); // New products are active by default

        Product savedProduct = productRepository.save(product);
        return mapToDto(savedProduct);
    }

    // This method is for the PUBLIC storefront and only shows active products
    public List<ProductDto> getActiveProducts(Integer categoryId) {
        List<Product> products;
        if (categoryId != null) {
            products = productRepository.findAllByCategoryIdAndActiveTrue(categoryId);
        } else {
            products = productRepository.findAllByActiveTrue();
        }
        return products.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // This method is for the ADMIN panel and shows ALL products
    public List<ProductDto> getAllProductsForAdmin() {
        return productRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public ProductDto getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        return mapToDto(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        // This now performs a "soft delete" by marking the product as inactive
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    public ProductDto updateProduct(Long productId, ProductDto productDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setStock(productDto.getStock());
        product.setImageUrl(productDto.getImageUrl());
        product.setCategory(category);
        product.setActive(productDto.isActive()); // Allow admin to reactivate a product

        Product updatedProduct = productRepository.save(product);
        return mapToDto(updatedProduct);
    }

    private ProductDto mapToDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory().getId())
                .active(product.isActive()) // Include the active status
                .build();
    }
}

