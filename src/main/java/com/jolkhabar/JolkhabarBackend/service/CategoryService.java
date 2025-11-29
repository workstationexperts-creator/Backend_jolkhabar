package com.jolkhabar.JolkhabarBackend.service;

import com.jolkhabar.JolkhabarBackend.dto.CategoryDto;
import com.jolkhabar.JolkhabarBackend.model.Category;
import com.jolkhabar.JolkhabarBackend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryDto addCategory(CategoryDto categoryDto) {
        Category category = new Category();
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        category.setImageUrl(categoryDto.getImageUrl());
        category.setBannerImageUrl(categoryDto.getBannerImageUrl());
        category.setLayoutType(categoryDto.getLayoutType());
        category.setActive(true); // New categories are active by default
        Category savedCategory = categoryRepository.save(category);
        return mapToDto(savedCategory);
    }

    // This method is for the PUBLIC storefront and only shows active categories
    public List<CategoryDto> getActiveCategories() {
        return categoryRepository.findAllByActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // This method is for the ADMIN panel and shows ALL categories
    public List<CategoryDto> getAllCategoriesForAdmin() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    public CategoryDto getCategoryById(Integer categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new RuntimeException("Category not found"));
        return mapToDto(category);
    }

    @Transactional
    public CategoryDto updateCategory(Integer categoryId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new RuntimeException("Category not found"));
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        category.setImageUrl(categoryDto.getImageUrl());
        category.setBannerImageUrl(categoryDto.getBannerImageUrl());
        category.setLayoutType(categoryDto.getLayoutType());
        category.setActive(categoryDto.isActive()); // Allow admin to reactivate a category
        Category updatedCategory = categoryRepository.save(category);
        return mapToDto(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Integer categoryId) {
        // This now performs a "soft delete"
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setActive(false);
        categoryRepository.save(category);
    }

    private CategoryDto mapToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .bannerImageUrl(category.getBannerImageUrl())
                .layoutType(category.getLayoutType())
                .active(category.isActive())
                .build();
    }
}

