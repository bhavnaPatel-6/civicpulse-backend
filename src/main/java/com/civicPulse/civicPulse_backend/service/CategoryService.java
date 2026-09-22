package com.civicPulse.civicPulse_backend.service;

import com.civicPulse.civicPulse_backend.dto.CategoryCreateRequest;
import com.civicPulse.civicPulse_backend.entity.Category;
import com.civicPulse.civicPulse_backend.repository.CategoryRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Admin: naya category banao
    public Category createCategory(CategoryCreateRequest request) {

        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Category already exists");
        }

        Category category = new Category(
                request.getName(),
                request.getDefaultDepartment()
        );

        return categoryRepository.save(category);
    }

    // Sabke liye: saari active categories dikhao
    // (Flutter app mein dropdown banane ke liye use hoga)
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findAll()
                .stream()
                .filter(Category::getActive)
                .toList();
    }

    public Category getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getActive()) {
            throw new RuntimeException("This category is no longer active");
        }

        return category;
    }
}